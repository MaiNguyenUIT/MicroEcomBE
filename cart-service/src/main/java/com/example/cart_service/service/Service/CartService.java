package com.example.cart_service.service.Service;

import com.example.cart_service.DTO.CartItemDTO;
import com.example.cart_service.DTO.CartResponse;
import com.example.cart_service.model.Cart;

public interface CartService {
    Cart addItemToUserCart(CartItemDTO cartItemDTO);
    CartResponse getCartByUserId();
    Cart updateItemQuantity(String productId, int quantity);
    Cart deleteItemFromCart(String productId);
    void clearUserCart();
    Cart increaseCartItem( String productId);
    Cart decreaseCartItem(String productId);
    Cart updateCartInfor(Cart cart);
}