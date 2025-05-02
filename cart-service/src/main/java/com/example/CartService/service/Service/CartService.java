package com.example.CartService.service.Service;

import com.example.CartService.DTO.CartItemDTO;
import com.example.CartService.model.Cart;

public interface CartService {
    Cart addItemToUserCart(String identifier, CartItemDTO cartItemDTO);
    Cart getCartByUserId(String userId);
    Cart updateItemQuantity(String userId, String productId, int quantity); // Tăng, giảm, hoặc xóa
    Cart deleteItemFromCart(String userId, String productId);
    void clearCart(String userId); // Xóa toàn bộ giỏ hàng
    Cart mergeGuestCartToUserCart(String sessionId, String userId); // Gộp giỏ hàng vãng lai vào tài khoản
    Cart increaseCartItem(String userId, String productId);
    Cart decreaseCartItem(String userId, String productId);
    Cart updateCartInfor(Cart cart);
}