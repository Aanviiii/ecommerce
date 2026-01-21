package com.example.ecommerce.service;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final PaymentService paymentService;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // Get cart items
        List<CartItem> cartItems = cartRepository.findByUserId(request.getUserId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create order items and calculate total
        double totalAmount = 0.0;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productService.getProductById(cartItem.getProductId());

            // Validate stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);
            totalAmount += product.getPrice() * cartItem.getQuantity();
        }

        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.CREATED.name());
        order.setCreatedAt(Instant.now());
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {} with total amount: {}", savedOrder.getId(), totalAmount);

        // Update stock
        for (OrderItem item : orderItems) {
            productService.updateStock(item.getProductId(), item.getQuantity());
        }

        // Clear cart
        cartRepository.deleteByUserId(request.getUserId());
        log.info("Cart cleared for user: {}", request.getUserId());

        return savedOrder;
    }

    public OrderResponse getOrderById(String orderId) {
        log.info("Fetching order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems());

        // Get payment info if exists
        paymentService.getPaymentByOrderId(orderId)
                .ifPresent(response::setPayment);

        return response;
    }

    public void updateOrderStatus(String orderId, String status) {
        log.info("Updating order {} status to: {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
    }

    public List<Order> getUserOrders(String userId) {
        log.info("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId);
    }
}
