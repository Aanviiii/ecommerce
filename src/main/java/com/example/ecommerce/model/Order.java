package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String userId;
    private Double totalAmount;
    private String status; // CREATED, PAID, FAILED, CANCELLED
    private Instant createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        CREATED, PAID, FAILED, CANCELLED
    }
}