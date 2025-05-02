package com.example.CartService.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.CartService.DTO.CartItemDTO;
import com.example.CartService.DTO.ProductDTO;
import com.example.CartService.client.ProductClient;
import com.example.CartService.exception.NotFoundException;
import com.example.CartService.mapper.CartItemMapper;
import com.example.CartService.model.*;
import com.example.CartService.repository.CartRepository;
import com.example.CartService.repository.GuestCartRepository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService implements com.example.CartService.service.Service.CartService {
@Autowired
private CartRepository cartRepository;
@Autowired
private GuestCartRepository guestCartRepository;

@Autowired
private ProductClient productClient;

@Override
public Cart addItemToUserCart(String userId, CartItemDTO cartItemDTO) {
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
public Cart getCartByUserId(String userId) {
    return cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
}

@Override
public Cart updateItemQuantity(String userId, String productId, int quantity) {
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
public Cart deleteItemFromCart(String userId, String productId) {
    Cart cart = cartRepository.findByuserId(userId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
    cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));
    return cartRepository.save(updateCartInfor(cart));
}

@Override
public void clearCart(String userId) {
    cartRepository.deleteByuserId(userId);
}

@Override
public Cart mergeGuestCartToUserCart(String sessionId, String userId) {
    GuestCart guestCart = guestCartRepository.findById(sessionId).orElse(null);
    Cart userCart = cartRepository.findByuserId(userId).orElse(new Cart());
    userCart.setUserId(userId);

    if (guestCart != null) {
        for (CartItem guestItem : guestCart.getCartItems()) {
            userCart.getCartItems().stream()
                    .filter(userItem -> userItem.getProductId().equals(guestItem.getProductId()))
                    .findFirst()
                    .ifPresentOrElse(
                            existingItem -> existingItem.setQuantity(existingItem.getQuantity() + guestItem.getQuantity()),
                            () -> userCart.getCartItems().add(guestItem)
                    );
        }
        guestCartRepository.deleteById(sessionId);
    }

    return cartRepository.save(updateCartInfor(userCart));
}

@Override
public Cart increaseCartItem(String userId, String productId) {
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
public Cart decreaseCartItem(String userId, String productId) {
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