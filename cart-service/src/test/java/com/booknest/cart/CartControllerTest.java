package com.booknest.cart;

import com.booknest.cart.controller.CartResource;
import com.booknest.cart.service.CartService;
import com.booknest.cart.entity.Cart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartResource.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    public void testGetCart() throws Exception {
        when(cartService.getCartByUser(1L)).thenReturn(new Cart());

        mockMvc.perform(get("/cart/1"))
                .andExpect(status().isOk());
    }
}
