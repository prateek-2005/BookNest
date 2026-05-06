package com.booknest.wishlist;

import com.booknest.wishlist.controller.WishlistResource;
import com.booknest.wishlist.service.WishlistService;
import com.booknest.wishlist.entity.Wishlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistResource.class)
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @Test
    public void testGetWishlist() throws Exception {
        when(wishlistService.getWishlistByUserId(1L)).thenReturn(new Wishlist());

        mockMvc.perform(get("/wishlist/1"))
                .andExpect(status().isOk());
    }
}
