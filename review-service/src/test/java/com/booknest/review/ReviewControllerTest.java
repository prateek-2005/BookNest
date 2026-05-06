package com.booknest.review;

import com.booknest.review.controller.ReviewResource;
import com.booknest.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewResource.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    public void testGetReviewsByBook() throws Exception {
        // Fix: Method name in ReviewService is getByBook
        when(reviewService.getByBook(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/reviews/book/1"))
                .andExpect(status().isOk());
    }
}
