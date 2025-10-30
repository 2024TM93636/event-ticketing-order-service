package com.ticket.order.service;

import com.ticket.order.dto.OrderRequest;
import com.ticket.order.model.Order;
import com.ticket.order.model.Ticket;
import com.ticket.order.repository.OrderRepository;
import com.ticket.order.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Creates a new order, calculates total (including 5% tax),
     * saves order and tickets, and returns the saved order.
     */
    public Order createOrder(OrderRequest request) {
        if (request.getUserId() == null || request.getEventId() == null) {
            throw new IllegalArgumentException("User ID and Event ID are required");
        }

        // Create base order
        Order order = Order.builder()
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .status("CREATED")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // Map incoming tickets
        List<Ticket> tickets = request.getTickets().stream()
                .map(t -> Ticket.builder()
                        .eventId(request.getEventId())
                        .seatId(t.getSeatId())
                        .pricePaid(t.getPrice())
                        .order(order)
                        .build())
                .toList();

        // Calculate totals
        double subtotal = tickets.stream()
                .mapToDouble(Ticket::getPricePaid)
                .sum();
        double tax = subtotal * 0.05;
        double total = subtotal + tax;

        order.setTickets(tickets);
        order.setOrderTotal(total);

        // Save order and tickets
        Order savedOrder = orderRepository.save(order);
        ticketRepository.saveAll(tickets);

        return savedOrder;
    }

    /** Fetch all orders */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /** Fetch order by ID */
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    /** Fetch all tickets belonging to an order */
    public List<Ticket> getTicketsByOrder(Long orderId) {
        return ticketRepository.findByOrderOrderId(orderId);
    }

    /** Cancel order */
    public Order cancelOrder(Long id) {
        Order order = getOrder(id);
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    /** Delete order */
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
