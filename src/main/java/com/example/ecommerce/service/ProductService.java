package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        log.info("Creating product: {}", product.getName());
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public void updateStock(String productId, int quantity) {
        Product product = getProductById(productId);
        int newStock = product.getStock() - quantity;

        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        product.setStock(newStock);
        productRepository.save(product);
        log.info("Updated stock for product {}: {} -> {}", productId, product.getStock() + quantity, newStock);
    }
}
