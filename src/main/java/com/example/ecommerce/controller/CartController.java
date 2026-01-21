package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.MessageResponse;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(@Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/cart/add - Adding to cart for user: {}", request.getUserId());
        CartItem cartItem = cartService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItemResponse>> getUserCart(@PathVariable String userId) {
        log.info("GET /api/cart/{} - Fetching cart", userId);
        List<CartItemResponse> cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<MessageResponse> clearCart(@PathVariable String userId) {
        log.info("DELETE /api/cart/{}/clear - Clearing cart", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(new MessageResponse("Cart cleared successfully"));
    }
}