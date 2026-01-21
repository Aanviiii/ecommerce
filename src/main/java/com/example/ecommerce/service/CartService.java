package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartItem addToCart(AddToCartRequest request) {
        log.info("Adding to cart - User: {}, Product: {}, Quantity: {}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        // Validate product exists and has stock
        Product product = productService.getProductById(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock available");
        }

        // Check if item already exists in cart
        return cartRepository.findByUserIdAndProductId(request.getUserId(), request.getProductId())
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                    log.info("Updated existing cart item: {}", existingItem.getId());
                    return cartRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUserId(request.getUserId());
                    newItem.setProductId(request.getProductId());
                    newItem.setQuantity(request.getQuantity());
                    log.info("Created new cart item");
                    return cartRepository.save(newItem);
                });
    }

    public List<CartItemResponse> getUserCart(String userId) {
        log.info("Fetching cart for user: {}", userId);
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        return cartItems.stream()
                .map(item -> {
                    CartItemResponse response = new CartItemResponse();
                    response.setId(item.getId());
                    response.setProductId(item.getProductId());
                    response.setQuantity(item.getQuantity());

                    try {
                        Product product = productService.getProductById(item.getProductId());
                        response.setProduct(product);
                    } catch (Exception e) {
                        log.error("Product not found: {}", item.getProductId());
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
    }
}