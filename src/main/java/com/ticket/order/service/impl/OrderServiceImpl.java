package com.ticket.order.service.impl;

import com.ticket.order.dto.OrderRequest;
import com.ticket.order.model.Order;
import com.ticket.order.model.Ticket;
import com.ticket.order.repository.OrderRepository;
import com.ticket.order.repository.TicketRepository;
import com.ticket.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public Order createOrder(OrderRequest request) {
        // Step 1: Calculate total (dummy price * 1.05 tax)
        BigDecimal basePrice = BigDecimal.valueOf(500); // assume seat price fixed for now
        BigDecimal total = basePrice
                .multiply(BigDecimal.valueOf(request.getSeatIds().size()))
                .multiply(BigDecimal.valueOf(1.05)); // add 5% tax

        // Step 2: Save order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Step 3: Create tickets
        List<Ticket> tickets = new ArrayList<>();
        for (Integer seatId : request.getSeatIds()) {
            Ticket ticket = new Ticket();
            ticket.setOrderId(savedOrder.getOrderId());
            ticket.setEventId(request.getEventId());
            ticket.setSeatId(seatId);
            ticket.setIssuedAt(LocalDateTime.now());
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);

        // Step 4: Mark order confirmed (for now)
        savedOrder.setStatus("CONFIRMED");
        return orderRepository.save(savedOrder);
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Ticket> getTickets(Long orderId) {
        return ticketRepository.findAll()
                .stream()
                .filter(t -> t.getOrderId().equals(orderId))
                .toList();
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

}
