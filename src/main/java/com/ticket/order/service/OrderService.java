package com.ticket.order.service;

import com.ticket.order.dto.OrderRequest;
import com.ticket.order.model.Order;
import com.ticket.order.model.Ticket;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequest request);
    Order cancelOrder(Long orderId);
    Order getOrder(Long orderId);
    List<Ticket> getTickets(Long orderId);
}
