/* package com.example.CartService.service.Service;

import com.example.CartService.DTO.CartItemDTO;
import com.example.CartService.model.GuestCart;

public interface GuestCartService {
    GuestCart addItemToGuestCart(String sessionId, CartItemDTO cartItemDTO);
    GuestCart getCartBySession(String sessionId); // Lấy giỏ hàng của khách vãng lai
    GuestCart updateItemQuantity(String sessionId, String productId, int quantity); // Tăng, giảm, hoặc xóa
    GuestCart deleteItemFromGuestCart(String sessionId, String productId);
    void clearCart(String sessionId); // Xóa toàn bộ giỏ hàng
    GuestCart increaseCartItem(String sessionId, String productId);
    GuestCart decreaseCartItem(String sessionId, String productId);
    GuestCart updateCartInfor(GuestCart guestCart);
}
 */