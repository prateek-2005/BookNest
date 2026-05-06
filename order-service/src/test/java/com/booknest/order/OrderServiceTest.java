package com.booknest.order;

import com.booknest.order.entity.Order;
import com.booknest.order.entity.Address;
import com.booknest.order.repository.OrderRepository;
import com.booknest.order.service.OrderServiceImpl;
import com.booknest.order.client.NotificationClient;
import com.booknest.order.client.AuthClient;
import com.booknest.order.client.WalletClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private WalletClient walletClient;
    @Mock
    private AuthClient authClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPlaceOrder_Success() {
        Order order = new Order();
        order.setUserId(1L);
        order.setQuantity(2);
        order.setModeOfPayment("COD");
        
        Address address = new Address();
        address.setCity("Mumbai");
        order.setAddress(address);
        
        order.setBook(new com.booknest.order.entity.Book());

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(authClient.getAdmins()).thenReturn(Collections.emptyList());

        Order result = orderService.placeOrder(order);
        assertNotNull(result);
        assertEquals("PLACED", result.getOrderStatus());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void testUpdateStatus_Success() {
        Order order = new Order();
        order.setOrderId(1L);
        order.setOrderStatus("PLACED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, "SHIPPED");
        assertEquals("SHIPPED", result.getOrderStatus());
    }
}
