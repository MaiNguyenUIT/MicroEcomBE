package com.example.cart_service.service.serviceImpl;

import com.example.cart_service.DTO.CartItemDTO;
import com.example.cart_service.DTO.CartResponse;
import com.example.cart_service.DTO.ProductDTO;
import com.example.cart_service.client.ProductClient;
import com.example.cart_service.exception.NotFoundException;
import com.example.cart_service.mapper.CartItemMapper;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                        () -> userCart.getCartItems().add(CartItemMapper.INSTANCE.toEntity(cartItemDTO))
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
    cartResponse.setProducts(productClient.getProductsByIds(productIds));
    cartResponse.setTotalItem(productIds.size());
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
public void clearCart() {
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
}