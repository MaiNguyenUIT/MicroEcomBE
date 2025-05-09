package com.example.product_service.service;

import com.example.product_service.ENUM.PRODUCT_STATE;
import com.example.product_service.dto.ProductDTO;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.event.StockUpdateEvent;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.mapper.ProductResMapper;
import com.example.product_service.model.Category;
import com.example.product_service.model.Product;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService{
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product existProduct = productRepository.findByname(productDTO.getName());
        if(existProduct != null){
            throw new BadRequestException("This product is already exist, please use update product");
        }
        if(productDTO.getQuantity() < 0){
            throw new BadRequestException("Product quantity must be more than 0");
        }
        if(productDTO.getPrice() < 0){
            throw new BadRequestException("Product price must be more than 0");
        }
        if(productDTO.getRegularPrice() < 0){
            throw new BadRequestException("Product regular price must be more than 0");
        }
        if(productDTO.getQuantity() > 0){
            productDTO.setProductState(PRODUCT_STATE.ACTIVE);
        } else {
            productDTO.setProductState(PRODUCT_STATE.HIDDEN);
        }


        Product product = ProductMapper.INSTANCE.toEntity(productDTO);
        product.setOwnerId(SecurityContextHolder.getContext().getAuthentication().getName());
        productRepository.save(product);

        return productDTO;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, String id) {
        Product existProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if(productRepository.existsByNameAndNotId(productDTO.getName(), id)){
            throw new BadRequestException("This product name is already exist");
        }
        if(productDTO.getQuantity() < 0){
            throw new BadRequestException("Product quantity must be more than 0");
        }
        if(productDTO.getPrice() < 0){
            throw new BadRequestException("Product price must be more than 0");
        }
        if(productDTO.getRegularPrice() < 0){
            throw new BadRequestException("Product regular price must be more than 0");
        }

        existProduct.setPrice(productDTO.getPrice());
        existProduct.setName(productDTO.getName());
        existProduct.setDescription(productDTO.getDescription());
        existProduct.setImage(productDTO.getImage());
        existProduct.setCategoryId(productDTO.getCategoryId());
        existProduct.setRegularPrice(productDTO.getRegularPrice());
        existProduct.setQuantity(productDTO.getQuantity());
        existProduct.setProductState(productDTO.getProductState());

        return ProductMapper.INSTANCE.toDTO(productRepository.save(existProduct));
    }

    @Override
    public void deleteProduct(String id) {
        Product existProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        existProduct.setProductState(PRODUCT_STATE.HIDDEN);
        productRepository.save(existProduct);
    }

    @Override
    public ProductResponse getProduct(String id) {
        Product existProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        ProductResponse productResponse = ProductResMapper.INSTANCE.toRes(existProduct);
        productResponse.setCategoryName(categoryRepository.findById(existProduct.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found")).getTitle());
        return productResponse;
    }

    @Override
    public List<ProductResponse> getAllProduct() {
        List<Product> products = productRepository.findAll();

        List<String> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        return products.stream().map(product -> {
            ProductResponse productResponse = ProductResMapper.INSTANCE.toRes(product);
            productResponse.setCategoryName(categoryMap.get(product.getCategoryId())); // Lấy tên category từ Map
            return productResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> filterProductByCategory(String categoryId) {
        List<Product> products = productRepository.findByCategoryIdAndProductStateAndIsApprove(
                categoryId, PRODUCT_STATE.ACTIVE, true
        );

        List<String> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        return products.stream().map(product -> {
            ProductResponse productResponse = ProductResMapper.INSTANCE.toRes(product);
            productResponse.setCategoryName(categoryMap.get(product.getCategoryId())); // Lấy tên category từ Map
            return productResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllActiveProduct() {
        List<Product> products = productRepository.findByProductStateAndIsApprove(PRODUCT_STATE.ACTIVE, true);;
        List<String> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        return products.stream().map(product -> {
            ProductResponse productResponse = ProductResMapper.INSTANCE.toRes(product);
            productResponse.setCategoryName(categoryMap.get(product.getCategoryId())); // Lấy tên category từ Map
            return productResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findProductsByOwnerId() {
        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Product> products = productRepository.findByownerId(ownerId);
        List<String> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        return products.stream().map(product -> {
            ProductResponse productResponse = ProductResMapper.INSTANCE.toRes(product);
            productResponse.setCategoryName(categoryMap.get(product.getCategoryId())); // Lấy tên category từ Map
            return productResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public Product approveProduct(String productId) {
        Product existProduct = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        existProduct.setApprove(true);
        return productRepository.save(existProduct);
    }

    @Override
    public List<ProductResponse> getProductInCart(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Product> products = productRepository.findAllById(productIds);
        List<String> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        return products.stream().map(product -> {
            ProductResponse productResponse = ProductResMapper.INSTANCE.toRes(product);
            productResponse.setCategoryName(categoryMap.get(product.getCategoryId())); // Lấy tên category từ Map
            return productResponse;
        }).collect(Collectors.toList());
    }

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
