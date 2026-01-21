package com.example.ecommerce.controller;

import com.example.ecommerce.dto.MessageResponse;
import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {
    private final PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<MessageResponse> handlePaymentWebhook(@RequestBody PaymentWebhookRequest request) {
        log.info("POST /api/webhooks/payment - Received webhook for order: {}", request.getOrderId());
        log.info("Payment status: {}", request.getStatus());

        paymentService.processWebhook(request);

        return ResponseEntity.ok(new MessageResponse("Webhook processed successfully"));
    }
}