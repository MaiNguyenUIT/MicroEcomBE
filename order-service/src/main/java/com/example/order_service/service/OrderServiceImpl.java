package com.example.order_service.service;

import com.example.order_service.DTO.*;
import com.example.order_service.ENUM.CouponType;
import com.example.order_service.ENUM.ORDER_STATUS;
import com.example.order_service.ENUM.PAYMENT_TYPE;
import com.example.order_service.client.CartClient;
import com.example.order_service.client.CouponClient;
import com.example.order_service.client.PaymentClient;
import com.example.order_service.client.ProductClient;
import com.example.order_service.client.UserClient;
import com.example.order_service.event.*;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.OrderTracker;
import com.example.order_service.model.CouponItem;
import com.example.order_service.model.ProductQuantity;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.OrderTrackerRepository;
import com.example.order_service.utils.CouponMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartClient cartClient;
    @Autowired
    private UserClient userClient;
    @Autowired
    private PaymentClient paymentClient;
    @Autowired
    private CouponClient couponClient;
    @Autowired
    private OrderTrackerRepository orderTrackerRepository;
    @Autowired
    private ProductClient productClient;

    private final StreamBridge streamBridge;

    public OrderServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public List<Order> createOrder(OrderDTO orderDTO) {
        CartDTO cart = cartClient.getUserCart();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty with user id: " + userId);
        }

        Map<String, List<CartItemDTO>> itemsBySeller = cart.getCartItems()
                .stream()
                .collect(Collectors.groupingBy(CartItemDTO::getOwnerId));

        List<Long> couponIds = orderDTO.getCouponIds();
        List<CouponItem> validCoupons = new ArrayList<>();

        if (!couponIds.isEmpty()) {
            CouponValidationResponse response = couponClient.getCouponsValidationResponses(
                    CouponMapperUtil.toCouponsRequest(couponIds)
            );

            if (!response.isSuccess()) {
                throw new BadRequestException("Invalid coupons provided");
            }

            validCoupons = response.getValidCoupons();
        }

        String orderGroupId = UUID.randomUUID().toString();
        List<Order> createdOrders = new ArrayList<>();

        for (Map.Entry<String, List<CartItemDTO>> entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<CartItemDTO> sellerItems = entry.getValue();
            Order order = buildOrder(orderDTO, userId, sellerId, orderGroupId);
            StockUpdateEvent stockUpdateEvent = new StockUpdateEvent();
            stockUpdateEvent.setOrderGroupId(orderGroupId);

            int total = buildOrderItems(order, sellerItems, stockUpdateEvent);

            BigDecimal totalDiscount = applyCoupons(order, validCoupons, sellerId, total);
            int finalAmount = BigDecimal.valueOf(total)
                    .multiply(BigDecimal.ONE.subtract(totalDiscount))
                    .intValue();
            order.setOrderAmount(finalAmount);

            Order savedOrder = orderRepository.save(order);
            stockUpdateEvent.setOrderId(savedOrder.getId());
            sendStockUpdate(stockUpdateEvent);

            OrderTracker orderTracker = buildOrderTracker(savedOrder.getId(), orderGroupId);
            orderTrackerRepository.save(orderTracker);

            createdOrders.add(savedOrder);
        }

        return createdOrders;
    }

    private Order buildOrder(OrderDTO dto, String userId, String sellerId, String groupId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setSellerId(sellerId);
        order.setOrderGroupId(groupId);
        order.setShippingAddress(dto.getShippingAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setOrderDateTime(dto.getOrderDateTime());
        order.setOrderItems(new ArrayList<>());
        order.setCouponIds(new ArrayList<>());
        return order;
    }

    @Override
    public Order createOrderDirectlyOffline(OrderDirectlyDTO orderDirectlyDTO) {
        ProductResponse productResponse = productClient.getProductById(orderDirectlyDTO.getProductId());
        if (productResponse == null){
            throw new NotFoundException("Product not found with id" + orderDirectlyDTO.getProductId());
        }
        String orderGroupId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setShippingAddress(orderDirectlyDTO.getShippingAddress());
        order.setSellerId(productResponse.getOwnerId());
        order.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        order.setPaymentMethod(orderDirectlyDTO.getPaymentMethod());
        order.setOrderDateTime(orderDirectlyDTO.getOrderDateTime());
        order.setOrderItems(new ArrayList<>());

        //Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(orderDirectlyDTO.getProductId());
        orderItem.setQuantity(orderDirectlyDTO.getProductQuantity());
        orderItem.setOrder(order);
        orderItem.setOrder(order);

        order.getOrderItems().add(orderItem);
        order.setOrderAmount(orderDirectlyDTO.getProductQuantity()*productResponse.getPrice());
        order.setOrderGroupId(orderGroupId);

        ProductQuantity productQuantity = new ProductQuantity();
        productQuantity.setQuantity(orderDirectlyDTO.getProductQuantity());
        productQuantity.setProductId(orderDirectlyDTO.getProductId());

        Order createdOrder = orderRepository.save(order);
        StockUpdateDirectlyEvent stockUpdateDirectlyEvent = new StockUpdateDirectlyEvent();
        stockUpdateDirectlyEvent.setOrderId(createdOrder.getId());
        stockUpdateDirectlyEvent.setProductQuantity(productQuantity);
        stockUpdateDirectlyEvent.setOrderGroupId(orderGroupId);

        streamBridge.send("stockUpdateDirectly-out-0", stockUpdateDirectlyEvent);

        return createdOrder;
    }

    @Override
    public String createOrderDirectlyOnline(OrderDirectlyDTO orderDirectlyDTO) {
        ProductResponse productResponse = productClient.getProductById(orderDirectlyDTO.getProductId());
        if (productResponse == null){
            throw new NotFoundException("Product not found with id" + orderDirectlyDTO.getProductId());
        }
        String orderGroupId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setShippingAddress(orderDirectlyDTO.getShippingAddress());
        order.setSellerId(productResponse.getOwnerId());
        order.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        order.setPaymentMethod(orderDirectlyDTO.getPaymentMethod());
        order.setOrderDateTime(orderDirectlyDTO.getOrderDateTime());
        order.setOrderItems(new ArrayList<>());

        //Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(orderDirectlyDTO.getProductId());
        orderItem.setQuantity(orderDirectlyDTO.getProductQuantity());
        orderItem.setOrder(order);
        orderItem.setOrder(order);

        order.getOrderItems().add(orderItem);
        order.setOrderAmount(orderDirectlyDTO.getProductQuantity()*productResponse.getPrice());
        order.setOrderGroupId(orderGroupId);

        ProductQuantity productQuantity = new ProductQuantity();
        productQuantity.setQuantity(orderDirectlyDTO.getProductQuantity());
        productQuantity.setProductId(orderDirectlyDTO.getProductId());

        Order createdOrder = orderRepository.save(order);

        //Create product reservation event;
        ProductReservationEvent productReservationEvent = new ProductReservationEvent(productQuantity.getProductId(),
                createdOrder.getId(), productQuantity.getQuantity());

        streamBridge.send("createProductReservation-out-0", productReservationEvent);

        //Create payment url
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setOrderId(createdOrder.getId());
        paymentDTO.setOrderAmount(order.getOrderAmount());
        paymentDTO.setPaymentType(PAYMENT_TYPE.PAYMENT_THEN_ORDER);
        return paymentClient.createPayment(paymentDTO);
    }
  
    private int buildOrderItems(Order order, List<CartItemDTO> items, StockUpdateEvent event) {
        int total = 0;
        for (CartItemDTO item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            order.getOrderItems().add(orderItem);

            total += item.getPrice();

            ProductQuantity pq = new ProductQuantity();
            pq.setProductId(item.getProductId());
            pq.setQuantity(item.getQuantity());
            event.getProductQuantities().add(pq);
        }
        return total;
    }

    private BigDecimal applyCoupons(Order order, List<CouponItem> validCoupons, String sellerId, int totalAmount) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        List<CouponItem> globalCoupons = validCoupons.stream()
                .filter(c -> c.getCouponType() == CouponType.GLOBAL)
                .toList();

        for (CouponItem global : globalCoupons) {
            if (BigDecimal.valueOf(totalAmount).compareTo(global.getMinPurchaseAmount()) >= 0) {
                order.getCouponIds().add(global.getCouponId());
                totalDiscount = totalDiscount.add(global.getDiscount());
            }
        }

        Optional<CouponItem> optionalCoupon = validCoupons.stream()
                .filter(c -> sellerId.equals(c.getCreatedByUserId()))
                .filter(c -> BigDecimal.valueOf(totalAmount).compareTo(c.getMinPurchaseAmount()) >= 0)
                .findFirst();

        if (optionalCoupon.isPresent()) {
            CouponItem c = optionalCoupon.get();
            totalDiscount = totalDiscount.add(c.getDiscount());
            order.getCouponIds().add(c.getCouponId());
        }


        return totalDiscount;
    }

    private OrderTracker buildOrderTracker(Long orderId, String groupId) {
        OrderTracker tracker = new OrderTracker();
        tracker.setId(orderId.toString() + groupId);
        tracker.setOrderId(orderId);
        tracker.setOrderGroupId(groupId);
        tracker.setStatus("fail" + groupId);
        return tracker;
    }

    public OrderUpdateStatusEvent buildUpdateEvent(UserDTO userDTO, Long orderId, ORDER_STATUS orderStatus){
        OrderUpdateStatusEvent orderUpdateStatusEvent = new OrderUpdateStatusEvent();
        orderUpdateStatusEvent.setId(orderId);
        orderUpdateStatusEvent.setOrderStatus(orderStatus);
        orderUpdateStatusEvent.setUserEmail(userDTO.getEmail());
        orderUpdateStatusEvent.setUserPhone(userDTO.getPhone());
        orderUpdateStatusEvent.setUserDisplayName(userDTO.getDisplayName());

        return orderUpdateStatusEvent;
    }

   
    public void sendStockUpdate(StockUpdateEvent event) {
        streamBridge.send("stockUpdate-out-0", event);
    }

    public void sendClearCart() {
        streamBridge.send("clearCart-out-0", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public void sendConfirmEmail(OrderConfirmEvent orderConfirmEvent) {
        streamBridge.send("sendConfirmOrderMail-out-0", orderConfirmEvent);
    }

    public void sendUpdateEmail(OrderUpdateStatusEvent orderUpdateStatusEvent){
        streamBridge.send("sendUpdateOrderMail-out-0", orderUpdateStatusEvent);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

    }

    @Override
    public List<Order> getOrderByUserId() {
        return orderRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        if (!order.getUserId().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new BadRequestException("You are not authorized to cancel this order: " + orderId);
        }
        if(order.getOrderStatus() != ORDER_STATUS.PENDING){
            throw new BadRequestException("Cannot cancel this order: " + orderId);
        } else {
            order.setOrderStatus(ORDER_STATUS.CANCELLED);
            return orderRepository.save(order);
        }
    }

    @Override
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrderStatus(Long orderId, ORDER_STATUS orderStatus) {
        UserDTO userDTO = userClient.getUserFromJwtToken();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        order.setOrderStatus(orderStatus);

        OrderUpdateStatusEvent orderUpdateStatusEvent = buildUpdateEvent(userDTO, orderId, orderStatus);
        sendUpdateEmail(orderUpdateStatusEvent);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrderBySellerId() {
        return orderRepository.findBySellerId(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public String onlinePaymentOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NotFoundException("Order not found with id" + orderId)
        );
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setOrderId(orderId);
        paymentDTO.setOrderAmount(order.getOrderAmount());
        paymentDTO.setPaymentType(PAYMENT_TYPE.ORDER_THEN_PAYMENT);
        return paymentClient.createPayment(paymentDTO);
    }
}
