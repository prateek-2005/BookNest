package com.booknest.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private LocalDate orderDate;
    private Double amountPaid;
    private String orderStatus;
    private String bookTitle;
    private Integer quantity;
}
