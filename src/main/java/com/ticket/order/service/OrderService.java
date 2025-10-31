package com.ticket.order.service;

import com.ticket.order.dto.OrderRequest;
import com.ticket.order.exception.BadRequestException;
import com.ticket.order.exception.ResourceNotFoundException;
import com.ticket.order.model.Order;
import com.ticket.order.model.Ticket;
import com.ticket.order.repository.OrderRepository;
import com.ticket.order.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    /** Create Order **/
    public Order createOrder(OrderRequest request) {
        validateOrderRequest(request);

        // Build base order
        Order order = Order.builder()
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .status("CREATED")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // Map and validate tickets
        List<Ticket> tickets = request.getTickets().stream()
                .map(t -> {
                    if (t.getSeatId() == null || t.getPrice() == null || t.getPrice() <= 0)
                        throw new BadRequestException("Each ticket must have a valid seatId and price");
                    return Ticket.builder()
                            .eventId(request.getEventId())
                            .seatId(t.getSeatId())
                            .pricePaid(t.getPrice())
                            .order(order)
                            .build();
                })
                .toList();

        double subtotal = tickets.stream().mapToDouble(Ticket::getPricePaid).sum();
        double tax = subtotal * 0.05;
        double total = subtotal + tax;

        order.setOrderTotal(total);
        order.setTickets(tickets);

        log.info("Creating order for user {} with {} tickets. Total = {}", request.getUserId(), tickets.size(), total);

        // Save and return
        Order savedOrder = orderRepository.save(order);
        ticketRepository.saveAll(tickets);
        return savedOrder;
    }

    /** Retrieve all orders **/
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /** Get order by ID **/
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID " + id));
    }

    /** Get tickets for an order **/
    public List<Ticket> getTicketsByOrder(Long orderId) {
        if (!orderRepository.existsById(orderId))
            throw new ResourceNotFoundException("Order not found with ID " + orderId);
        return ticketRepository.findByOrderOrderId(orderId);
    }

    /** Cancel order **/
    public Order cancelOrder(Long id) {
        Order order = getOrder(id);
        if ("CANCELLED".equals(order.getStatus()))
            throw new BadRequestException("Order is already cancelled");
        order.setStatus("CANCELLED");
        order.setPaymentStatus("REFUND_PENDING");
        log.warn("Order {} has been cancelled.", id);
        return orderRepository.save(order);
    }

    /** Delete order **/
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id))
            throw new ResourceNotFoundException("Cannot delete: Order not found with ID " + id);
        orderRepository.deleteById(id);
        log.info("Order {} deleted successfully.", id);
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request.getUserId() == null)
            throw new BadRequestException("User ID is required");
        if (request.getEventId() == null)
            throw new BadRequestException("Event ID is required");
        if (request.getTickets() == null || request.getTickets().isEmpty())
            throw new BadRequestException("At least one ticket is required to create an order");
    }
}
