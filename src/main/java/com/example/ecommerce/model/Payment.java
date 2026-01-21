package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String orderId;
    private Double amount;
    private String status; // PENDING, SUCCESS, FAILED
    private String paymentId; // External payment gateway ID
    private Instant createdAt;

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED
    }
}