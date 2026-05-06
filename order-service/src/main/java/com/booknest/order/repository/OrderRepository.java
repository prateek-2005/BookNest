package com.booknest.order.repository;

import com.booknest.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByOrderStatus(String status);
    List<Order> findByOrderDateBetween(LocalDate start, LocalDate end);
}
