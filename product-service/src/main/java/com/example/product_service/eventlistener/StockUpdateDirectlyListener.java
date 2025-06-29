package com.example.product_service.eventlistener;

import com.example.product_service.ENUM.PRODUCT_RESERVATION_STATE;
import com.example.product_service.ENUM.PRODUCT_STATE;
import com.example.product_service.event.AfterStockUpdateEvent;
import com.example.product_service.event.ProductReservationEvent;
import com.example.product_service.event.ReleaseProductReservationEvent;
import com.example.product_service.event.StockUpdateDirectlyEvent;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.model.Product;
import com.example.product_service.model.ProductQuantity;
import com.example.product_service.model.ProductStockReservation;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ProductStockReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class StockUpdateDirectlyListener {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockReservationRepository productStockReservationRepository;

    private final StreamBridge streamBridge;

    public StockUpdateDirectlyListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    Consumer<StockUpdateDirectlyEvent> stockUpdateDirectly() {
        return event -> {
            ProductQuantity item = event.getProductQuantity();
            Product product = productRepository.findById(item.getProductId()).orElseThrow(
                    () -> new NotFoundException("Product is not found with id: " + item.getProductId())
            );

            //Create event to update order status
            AfterStockUpdateEvent afterStockUpdateEvent = new AfterStockUpdateEvent();
            afterStockUpdateEvent.setOrderGroupId(event.getOrderGroupId());
            afterStockUpdateEvent.setOrderId(event.getOrderId());

            if(product.getQuantity() < item.getQuantity()){
                streamBridge.send("stockUpdateFail-out-0", afterStockUpdateEvent);
                throw new BadRequestException("Product is not enough with id: " + product.getId());
            }

            List<ProductStockReservation> productStockReservations =
                    productStockReservationRepository.findByProductId(product.getId());
            int totalReservation = 0;
            if(!productStockReservations.isEmpty()){
                for(ProductStockReservation productStockReservation : productStockReservations){
                    if(productStockReservation.getState().equals(PRODUCT_RESERVATION_STATE.HELD)){
                        totalReservation += productStockReservation.getQuantity();
                    }
                }
            }
            if(product.getQuantity() < (totalReservation + item.getQuantity())){
                streamBridge.send("stockUpdateFail-out-0", afterStockUpdateEvent);
                throw new BadRequestException("Product is not enough with id: " + product.getId());
            }

            int afterQuantity = product.getQuantity() - item.getQuantity();
            product.setQuantity(afterQuantity);
            product.setSold(product.getSold() + item.getQuantity());
            if ((afterQuantity - totalReservation) == 0) {
                product.setProductState(PRODUCT_STATE.HIDDEN);
            }
            productRepository.save(product);
            streamBridge.send("stockUpdateDirectlySuccess-out-0", afterStockUpdateEvent);
        };
    }

    @Bean
    Consumer<ProductReservationEvent> createProductReservation() {
        return event -> {
            Product product = productRepository.findById(event.getProductId()).orElseThrow(
                    () -> new NotFoundException("Product is not found with id: " + event.getProductId())
            );

            List<ProductStockReservation> productStockReservations =
                    productStockReservationRepository.findByProductId(product.getId());
            int totalReservation = 0;
            if(!productStockReservations.isEmpty()){
                for(ProductStockReservation productStockReservation : productStockReservations){
                    if(productStockReservation.getState().equals(PRODUCT_RESERVATION_STATE.HELD)){
                        totalReservation += productStockReservation.getQuantity();
                    }
                }
                AfterStockUpdateEvent afterStockUpdateEvent = new AfterStockUpdateEvent();
                afterStockUpdateEvent.setOrderId(event.getOrderId());
                if(product.getQuantity() < (totalReservation + event.getQuantity())){
                    streamBridge.send("stockUpdateFail-out-0", afterStockUpdateEvent);
                };
            }

            ProductStockReservation productStockReservation = new ProductStockReservation();
            productStockReservation.setProductId(event.getProductId());
            productStockReservation.setOrderId(event.getOrderId());
            productStockReservation.setQuantity(event.getQuantity());
            productStockReservation.setState(PRODUCT_RESERVATION_STATE.HELD);
            productStockReservation.setId(event.getOrderId() + event.getProductId());

            productStockReservationRepository.save(productStockReservation);
        };
    }

    @Bean
    public Consumer<ReleaseProductReservationEvent> releaseProductReservation() {
        return event -> {
            ProductStockReservation productStockReservation =
                    productStockReservationRepository.findById(event.getOrderId()+event.getProductId())
                            .orElseThrow();

            productStockReservation.setState(PRODUCT_RESERVATION_STATE.RELEASE);
            Product product = productRepository.findById(event.getProductId()).orElseThrow(
                    () -> new NotFoundException("Product is not found with id: " + event.getProductId())
            );

            product.setQuantity(product.getQuantity() - productStockReservation.getQuantity());
            product.setSold(product.getSold() + productStockReservation.getQuantity());
            productRepository.save(product);

            AfterStockUpdateEvent afterStockUpdateEvent = new AfterStockUpdateEvent();
            afterStockUpdateEvent.setOrderId(event.getOrderId());
            afterStockUpdateEvent.setOrderGroupId(UUID.randomUUID().toString());
            streamBridge.send("stockUpdateDirectlySuccess-out-0", afterStockUpdateEvent);
        };
    }
}
