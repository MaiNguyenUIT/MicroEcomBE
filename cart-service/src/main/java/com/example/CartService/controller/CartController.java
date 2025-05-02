package com.example.CartService.controller;


import com.example.CartService.ApiResult.ApiResult;
import com.example.CartService.DTO.CartItemDTO;
import com.example.CartService.client.UserClient;
import com.example.CartService.model.Cart;
import com.example.CartService.model.User;
import com.example.CartService.service.Service.CartService;
import com.example.CartService.utils.MapResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private MapResult mapResult;

    @PostMapping()
    private ResponseEntity<ApiResult<Cart>> addItemToUserCart(@RequestHeader("Authorization") String jwt, @RequestBody CartItemDTO cartItemDTO) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> apiResult = mapResult.map(cartService.addItemToUserCart(user.getId(), cartItemDTO), "Add " + cartItemDTO.getProductId() + " to cart successfully");
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }
    
    @PostMapping("/merge/{sessionId}")
    public ResponseEntity<ApiResult<Cart>> mergeGuestToUser(@RequestHeader("Authorization") String jwt,
                                                             @PathVariable String sessionId) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> result = mapResult.map(
            cartService.mergeGuestCartToUserCart(sessionId, user.getId()),
            "Merge cart successfully"
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResult<Cart>> getUserCart(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> result = mapResult.map(
            cartService.getCartByUserId(user.getId()),
            "Get user cart successfully"
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<String> clearUserCart(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        cartService.clearCart(user.getId());
        return new ResponseEntity<>("Clear cart successfully", HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<ApiResult<Cart>> deleteItemFromCart(@RequestHeader("Authorization") String jwt,
                                                               @RequestParam String productId) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> result = mapResult.map(
            cartService.deleteItemFromCart(user.getId(), productId),
            "Delete item with id: " + productId + " successfully"
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/quantity")
    public ResponseEntity<ApiResult<Cart>> updateItemQuantity(@RequestHeader("Authorization") String jwt,
                                                               @RequestParam String productId,
                                                               @RequestBody int quantity) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> result = mapResult.map(
            cartService.updateItemQuantity(user.getId(), productId, quantity),
            "Update item quantity successfully"
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/increase")
    public ResponseEntity<ApiResult<Cart>> increaseItemQuantity(@RequestHeader("Authorization") String jwt,
                                                                 @RequestParam String productId) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> result = mapResult.map(
            cartService.increaseCartItem(user.getId(), productId),
            "Increase product quantity successfully"
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/decrease")
    public ResponseEntity<ApiResult<Cart>> decreaseItemQuantity(@RequestHeader("Authorization") String jwt,
                                                                 @RequestParam String productId) throws Exception {
        User user = userClient.getUserFromToken(jwt);
        ApiResult<Cart> result = mapResult.map(
            cartService.decreaseCartItem(user.getId(), productId),
            "Decrease product quantity successfully"
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}