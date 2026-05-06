package com.booknest.order.service;

import com.booknest.order.client.AuthClient;
import com.booknest.order.client.NotificationClient;
import com.booknest.order.client.WalletClient;
import com.booknest.order.entity.Order;
import com.booknest.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private WalletClient walletClient;

    @Autowired
    private AuthClient authClient;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> getOrdersByOrderStatus(String status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Override
    public List<Order> getOrdersByOrderDateBetween(LocalDate start, LocalDate end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }

    @Override
    @Transactional
    public Order placeOrder(Order order) {
        log.info("[PLACE-ORDER] Starting order placement for user: {} with payment mode: {}", order.getUserId(), order.getModeOfPayment());
        if (order.getAddress() == null || order.getAddress().getCity() == null || order.getAddress().getCity().trim().isEmpty()) {
            log.error("[PLACE-ORDER] Validation failed: Missing address or city");
            throw new RuntimeException("Delivery address is required");
        }

        order.setOrderDate(LocalDate.now());
        order.setOrderStatus("PLACED");

        Order saved = orderRepository.save(order);
        log.info("[PLACE-ORDER] Order saved to database with ID: {}", saved.getOrderId());

        if ("WALLET".equalsIgnoreCase(saved.getModeOfPayment())) {
            log.info("[PLACE-ORDER] Processing wallet payment for user: {}, amount: {}", saved.getUserId(), saved.getAmountPaid());
            walletClient.debitForOrder(saved.getUserId(), saved.getAmountPaid(), saved.getOrderId());
            saved = orderRepository.save(saved);
            try {
                notificationClient.sendInAppNotification(
                        saved.getUserId(),
                        "PAYMENT_CONFIRMED",
                        "Wallet payment successful for order #" + saved.getOrderId() + "."
                );
            } catch (Exception e) {
                log.warn("[GRACEFUL-DEGRADATION] In-app notification failed: {}", e.getMessage());
            }
        }

        try {
            notificationClient.sendInAppNotification(
                    saved.getUserId(),
                    "ORDER_PLACED",
                    "Your order #" + saved.getOrderId() + " has been placed successfully."
            );
            notificationClient.sendEmailNotification(
                    saved.getUserId(),
                    "ORDER_PLACED",
                    "Your order #" + saved.getOrderId() + " has been placed successfully."
            );
        } catch (Exception e) {
            log.warn("[GRACEFUL-DEGRADATION] User notification failed: {}", e.getMessage());
        }

        // Notify Admins
        try {
            List<Map<String, Object>> admins = authClient.getAdmins();
            for (Map<String, Object> admin : admins) {
                Object rawId = admin.get("userId");
                if (rawId != null) {
                    Long adminId = Long.valueOf(rawId.toString());
                    notificationClient.sendInAppNotification(
                        adminId,
                        "NEW_ORDER_ADMIN",
                        "New order #" + saved.getOrderId() + " placed by user ID " + saved.getUserId() + "."
                    );
                }
            }
        } catch (Exception e) {
            log.warn("[NOTIFICATION-ERROR] Failed to notify admins: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    public Order onlinePayment(Order order) {
        if (order.getAddress() == null || order.getAddress().getCity() == null || order.getAddress().getCity().trim().isEmpty()) {
            throw new RuntimeException("Delivery address is required");
        }

        order.setOrderDate(LocalDate.now());
        order.setModeOfPayment("ONLINE");
        order.setOrderStatus("PLACED");

        Order saved = orderRepository.save(order);

        try {
            notificationClient.sendInAppNotification(
                    saved.getUserId(),
                    "PAYMENT_CONFIRMED",
                    "Payment confirmed for order #" + saved.getOrderId() + "."
            );
            notificationClient.sendEmailNotification(
                    saved.getUserId(),
                    "PAYMENT_CONFIRMED",
                    "Payment confirmed for order #" + saved.getOrderId() + "."
            );
        } catch (Exception e) {
            log.warn("[GRACEFUL-DEGRADATION] Notification service failed: {}", e.getMessage());
        }

        try {
            List<Map<String, Object>> admins = authClient.getAdmins();
            for (Map<String, Object> admin : admins) {
                Number adminId = (Number) admin.get("userId");
                if (adminId != null) {
                    notificationClient.sendInAppNotification(
                        adminId.longValue(),
                        "NEW_ORDER_ADMIN",
                        "New online order #" + saved.getOrderId() + " placed."
                    );
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        return saved;
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);

        try {
            notificationClient.sendInAppNotification(
                    order.getUserId(),
                    "ORDER_CANCELLED",
                    "Order #" + order.getOrderId() + " has been cancelled."
            );
            notificationClient.sendEmailNotification(
                    order.getUserId(),
                    "ORDER_CANCELLED",
                    "Order #" + order.getOrderId() + " has been cancelled."
            );
        } catch (Exception e) {
            log.warn("[GRACEFUL-DEGRADATION] Notification service failed: {}", e.getMessage());
        }
        
        notifyAdmins("ORDER_CANCELLED", "Order #" + order.getOrderId() + " was cancelled by user ID " + order.getUserId() + ".");
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(status);
        Order saved = orderRepository.save(order);
        try {
            notificationClient.sendInAppNotification(
                    saved.getUserId(),
                    "ORDER_STATUS_UPDATED",
                    "Order #" + saved.getOrderId() + " status updated to " + saved.getOrderStatus() + "."
            );
            notificationClient.sendEmailNotification(
                    saved.getUserId(),
                    "ORDER_STATUS_UPDATED",
                    "Order #" + saved.getOrderId() + " status updated to " + saved.getOrderStatus() + "."
            );
        } catch (Exception e) {
            log.warn("[GRACEFUL-DEGRADATION] Notification service failed: {}", e.getMessage());
        }
        
        if ("RETURNED".equalsIgnoreCase(status)) {
            notifyAdmins("ORDER_RETURNED", "Order #" + saved.getOrderId() + " was returned by user ID " + saved.getUserId() + ".");
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            notifyAdmins("ORDER_CANCELLED", "Order #" + saved.getOrderId() + " was cancelled by user ID " + saved.getUserId() + ".");
        }
        
        return saved;
    }

    private void notifyAdmins(String type, String message) {
        try {
            List<Map<String, Object>> admins = authClient.getAdmins();
            for (Map<String, Object> admin : admins) {
                Object rawId = admin.get("userId");
                if (rawId != null) {
                    Long adminId = Long.valueOf(rawId.toString());
                    notificationClient.sendInAppNotification(adminId, type, message);
                }
            }
        } catch (Exception e) {
            log.warn("[NOTIFICATION-ERROR] Failed to notify admins: {}", e.getMessage());
        }
    }
}
