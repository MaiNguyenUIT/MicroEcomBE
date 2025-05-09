package com.example.cart_service.service.serviceImpl;

import com.example.cart_service.DTO.CartItemDTO;
import com.example.cart_service.DTO.CartItemResponse;
import com.example.cart_service.DTO.CartResponse;
import com.example.cart_service.DTO.ProductDTO;
import com.example.cart_service.client.ProductClient;
import com.example.cart_service.exception.NotFoundException;
import com.example.cart_service.mapper.CartItemMapper;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService implements com.example.cart_service.service.Service.CartService {

@Autowired
private CartRepository cartRepository;
@Autowired
private ProductClient productClient;

@Override
public Cart addItemToUserCart(CartItemDTO cartItemDTO) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    Cart userCart = cartRepository.findByuserId(userId).orElse(null);
    ProductDTO product = productClient.getProductById(cartItemDTO.getProductId());

    if (userCart == null) {
        Cart cart = new Cart();
        cart.getCartItems().add(CartItemMapper.INSTANCE.toEntity(cartItemDTO));
        cart.setTotalItem(1);
        cart.setTotalPrice(product.getPrice());
        cart.setUserId(userId);
        return cartRepository.save(cart);
    } else {
        userCart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(cartItemDTO.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> existingItem.setQuantity(existingItem.getQuantity() + 1),
                        () -> {
                            CartItem newCartItem = CartItemMapper.INSTANCE.toEntity(cartItemDTO);
                            newCartItem.setOwnerId(product.getOwnerId());
                            userCart.getCartItems().add(newCartItem);
                        }
                );

        return cartRepository.save(updateCartInfor(userCart));
    }
}

@Override
public CartResponse getCartByUserId() {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    Cart cart = cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
    CartResponse cartResponse = new CartResponse();
    cartResponse.setUserId(userId);
    cartResponse.setTotalPrice(cart.getTotalPrice());
    cartResponse.setId(cart.getId());
    List<String> productIds = cart.getCartItems().stream()
            .map(CartItem::getProductId)
            .distinct()
            .toList();
    List<ProductDTO> productDTOS = productClient.getProductsByIds(productIds);
    
    Map<String, ProductDTO> productMap = productDTOS.stream()
            .collect(Collectors.toMap(ProductDTO::getId, Function.identity()));

    List<CartItemResponse> cartItemResponses = new ArrayList<>();

    for (CartItem cartItem : cart.getCartItems()) {
        ProductDTO productDTO = productMap.get(cartItem.getProductId());
        if (productDTO != null) {
            CartItemResponse cartItemResponse = new CartItemResponse();
            cartItemResponse.setImage(productDTO.getImage());
            cartItemResponse.setPrice(productDTO.getPrice());
            cartItemResponse.setProductCategory(productDTO.getCategoryName());
            cartItemResponse.setRegularPrice(productDTO.getRegularPrice());
            cartItemResponse.setDescription(productDTO.getDescription());
            cartItemResponse.setProductId(productDTO.getId());
            cartItemResponse.setProductName(productDTO.getName());
            cartItemResponse.setOwnerId(productDTO.getOwnerId());

            cartItemResponse.setQuantity(cartItem.getQuantity());
            cartItemResponse.setAddedAt(cartItem.getAddedAt());
            cartItemResponses.add(cartItemResponse);
        }
    }
    int totalItem = 0;
    for (CartItem cartItem : cart.getCartItems()){
        totalItem += cartItem.getQuantity();
    }
    cartResponse.setCartItems(cartItemResponses);
    cartResponse.setTotalItem(totalItem);
    return cartResponse;
}

@Override
public Cart updateItemQuantity(String productId, int quantity) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    Cart cart = cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
    Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();

    if (cartItemOptional.isPresent()) {
        CartItem cartItem = cartItemOptional.get();
        if (quantity > 0) {
            cartItem.setQuantity(quantity);
        } else {
            cart.getCartItems().remove(cartItem);
        }
    } else {
        throw new NotFoundException("Product not found in cart");
    }

    return cartRepository.save(updateCartInfor(cart));
}

@Override
public Cart deleteItemFromCart(String productId) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    Cart cart = cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
    cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));
    return cartRepository.save(updateCartInfor(cart));
}

@Override
public void clearUserCart() {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    cartRepository.deleteByuserId(userId);
}


@Override
public Cart increaseCartItem(String productId) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    Cart cart = cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));

    Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();

    if (cartItemOptional.isPresent()) {
        CartItem cartItem = cartItemOptional.get();
        cartItem.setQuantity(cartItem.getQuantity() + 1);
    } else {
        throw new NotFoundException("Product not found in cart");
    }

    return cartRepository.save(updateCartInfor(cart));
}

@Override
public Cart decreaseCartItem(String productId) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    Cart cart = cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));

    Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();

    if (cartItemOptional.isPresent()) {
        CartItem cartItem = cartItemOptional.get();
        int newQuantity = cartItem.getQuantity() - 1;

        if (newQuantity > 0) {
            cartItem.setQuantity(newQuantity);
        } else {
            cart.getCartItems().remove(cartItem);
        }
    } else {
        throw new NotFoundException("Product not found in cart");
    }

    return cartRepository.save(updateCartInfor(cart));
}

@Override
public Cart updateCartInfor(Cart cart) {
    List<String> productIds = cart.getCartItems().stream()
            .map(CartItem::getProductId)
            .collect(Collectors.toList());

    List<ProductDTO> products = productClient.getProductsByIds(productIds);

    Map<String, Integer> productPriceMap = products.stream()
            .collect(Collectors.toMap(ProductDTO::getId, ProductDTO::getPrice));

    int totalPrice = 0;
    int totalItem = 0;

    for (CartItem i : cart.getCartItems()) {
        totalItem += i.getQuantity();
        totalPrice += productPriceMap.getOrDefault(i.getProductId(), 0) * i.getQuantity();
    }

    cart.setTotalItem(totalItem);
    cart.setTotalPrice(totalPrice);
    return cart;
}

@Bean
public Consumer<String> clearCart(){
    return clearString -> {
        cartRepository.deleteByuserId(clearString);
        System.out.println("📥 Nhận message từ RabbitMQ: " + clearString);
    };
}
}