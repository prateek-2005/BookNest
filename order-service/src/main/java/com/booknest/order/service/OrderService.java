package com.booknest.order.service;

import com.booknest.order.entity.Order;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByOrderStatus(String status);
    List<Order> getOrdersByOrderDateBetween(LocalDate start, LocalDate end);
    Order placeOrder(Order order);
    Order onlinePayment(Order order);
    void cancelOrder(Long orderId);
    Order updateOrderStatus(Long orderId, String status);
}
