package com.ticket.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private Long eventId;
    private List<TicketRequest> tickets;

    @Data
    public static class TicketRequest {
        private Long seatId;
        private Double price;
    }
}
