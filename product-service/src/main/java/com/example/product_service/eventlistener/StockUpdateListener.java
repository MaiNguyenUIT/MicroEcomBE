package com.example.product_service.eventlistener;

import com.example.product_service.ENUM.PRODUCT_STATE;
import com.example.product_service.event.StockUpdateEvent;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class StockUpdateListener {
    @Autowired
    private ProductRepository productRepository;

    @Bean
    public Consumer<StockUpdateEvent> stockUpdate(){
        return event -> {
            System.out.println("Stock update");
            Product product = productRepository.findById(event.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < event.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            int afterQuantity = product.getQuantity() - event.getQuantity();
            product.setQuantity(afterQuantity);
            product.setSold(product.getSold() + event.getQuantity());

            if (afterQuantity == 0) {
                product.setProductState(PRODUCT_STATE.HIDDEN);
            }

            productRepository.save(product);
        };
    }
}
