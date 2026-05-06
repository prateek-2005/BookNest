package com.booknest.order;

import com.booknest.order.controller.OrderResource;
import com.booknest.order.service.OrderService;
import com.booknest.order.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderResource.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    public void testGetAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(new Order()));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOrdersByUser() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(Arrays.asList(new Order()));

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk());
    }
}
