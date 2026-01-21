package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment createPayment(PaymentRequest request) {
        log.info("Creating payment for order: {}", request.getOrderId());

        // Validate order exists and is in CREATED status
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        if (!Order.OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new RuntimeException("Order is not in CREATED status");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus(Payment.PaymentStatus.PENDING.name());
        payment.setPaymentId("pay_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setCreatedAt(Instant.now());

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created: {} for order: {}", savedPayment.getId(), request.getOrderId());

        return savedPayment;
    }

    public void processWebhook(PaymentWebhookRequest webhookRequest) {
        log.info("Processing payment webhook for order: {}", webhookRequest.getOrderId());

        // Find payment by order ID
        Payment payment = paymentRepository.findByOrderId(webhookRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + webhookRequest.getOrderId()));

        // Update payment status
        payment.setStatus(webhookRequest.getStatus());
        if (webhookRequest.getPaymentId() != null) {
            payment.setPaymentId(webhookRequest.getPaymentId());
        }
        paymentRepository.save(payment);

        // Update order status based on payment status
        Order order = orderRepository.findById(webhookRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + webhookRequest.getOrderId()));

        if ("SUCCESS".equals(webhookRequest.getStatus())) {
            order.setStatus(Order.OrderStatus.PAID.name());
            log.info("Order {} marked as PAID", order.getId());
        } else if ("FAILED".equals(webhookRequest.getStatus())) {
            order.setStatus(Order.OrderStatus.FAILED.name());
            log.info("Order {} marked as FAILED", order.getId());
        }

        orderRepository.save(order);
    }

    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}