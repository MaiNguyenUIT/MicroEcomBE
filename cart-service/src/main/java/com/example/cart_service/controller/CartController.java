package com.example.cart_service.controller;

import com.example.cart_service.DTO.CartItemDTO;
import com.example.cart_service.DTO.CartResponse;
import com.example.cart_service.model.Cart;
import com.example.cart_service.service.Service.CartService;
import com.example.cart_service.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
@RestController
@RequestMapping("api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @PostMapping()
    private ResponseEntity<ApiResponse<Cart>> addItemToUserCart(@RequestBody CartItemDTO cartItemDTO) throws Exception {
        Cart cart = cartService.addItemToUserCart(cartItemDTO);
        ApiResponse<Cart> apiResponse = ApiResponse.<Cart>builder()
                .status(200)
                .data(cart)
                .message("Add item to cart successfully")
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<CartResponse> getUserCart() throws Exception {
        return new ResponseEntity<>(cartService.getCartByUserId(), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<String> clearUserCart() throws Exception {
        cartService.clearUserCart();
        return new ResponseEntity<>("Clear cart successfully", HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<ApiResponse<Cart>> deleteItemFromCart(@RequestParam String productId) throws Exception {
        Cart cart = cartService.deleteItemFromCart(productId);
        ApiResponse<Cart> apiResponse = ApiResponse.<Cart>builder()
                .status(200)
                .data(cart)
                .message("Delete item with id: " + productId + " successfully")
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/quantity")
    public ResponseEntity<Cart> updateItemQuantity(@RequestParam String productId,
                                                   @RequestBody int quantity) throws Exception {
        return new ResponseEntity<>(cartService.updateItemQuantity(productId, quantity), HttpStatus.OK);
    }

    @PutMapping("/increase")
    public ResponseEntity<Cart> increaseItemQuantity(@RequestParam String productId) throws Exception {
        return new ResponseEntity<>(cartService.increaseCartItem(productId), HttpStatus.OK);
    }

    @PutMapping("/decrease")
    public ResponseEntity<Cart> decreaseItemQuantity(@RequestParam String productId) throws Exception {
        return new ResponseEntity<>(cartService.decreaseCartItem(productId), HttpStatus.OK);
    }

}