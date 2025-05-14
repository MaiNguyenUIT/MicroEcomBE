package com.example.product_service.eventlistener;

import com.example.product_service.ENUM.PRODUCT_STATE;
import com.example.product_service.event.AfterStockUpdateEvent;
import com.example.product_service.event.StockUpdateEvent;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.model.Product;
import com.example.product_service.model.ProductQuantity;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class StockUpdateListener {
    @Autowired
    private ProductRepository productRepository;

    private final StreamBridge streamBridge;

    public StockUpdateListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<StockUpdateEvent> stockUpdate() {
        return event -> {
            System.out.println("Stock update");

            List<ProductQuantity> items = event.getProductQuantities();
            List<String> insufficientProducts = new ArrayList<>();
            Map<String, Product> productMap = new HashMap<>();

            for (ProductQuantity item : items) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product == null) {
                    insufficientProducts.add(item.getProductId());
                    continue;
                }

                if (product.getQuantity() < item.getQuantity()) {
                    insufficientProducts.add(item.getProductId());
                } else {
                    productMap.put(item.getProductId(), product);
                }
            }
            AfterStockUpdateEvent afterStockUpdateEvent = new AfterStockUpdateEvent();
            afterStockUpdateEvent.setOrderGroupId(event.getOrderGroupId());
            afterStockUpdateEvent.setOrderId(event.getOrderId());

            if (!insufficientProducts.isEmpty()) {
                streamBridge.send("stockUpdateFail-out-0", afterStockUpdateEvent);
                return;
            }

            for (ProductQuantity item : items) {
                Product product = productMap.get(item.getProductId());
                int afterQuantity = product.getQuantity() - item.getQuantity();

                product.setQuantity(afterQuantity);
                product.setSold(product.getSold() + item.getQuantity());

                if (afterQuantity == 0) {
                    product.setProductState(PRODUCT_STATE.HIDDEN);
                }

                productRepository.save(product);
            }

            streamBridge.send("stockUpdateSuccess-out-0", afterStockUpdateEvent);
        };
    }


}
