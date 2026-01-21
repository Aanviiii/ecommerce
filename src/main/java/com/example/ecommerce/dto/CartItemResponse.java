package com.example.ecommerce.dto;

import com.example.ecommerce.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String id;
    private String productId;
    private Integer quantity;
    private Product product;
}