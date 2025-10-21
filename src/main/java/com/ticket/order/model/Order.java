package com.ticket.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private Integer userId;
    private BigDecimal totalAmount;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private LocalDateTime createdAt;
}
