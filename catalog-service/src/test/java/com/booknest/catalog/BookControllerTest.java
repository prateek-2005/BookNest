package com.booknest.catalog;

import com.booknest.catalog.controller.BookResource;
import com.booknest.catalog.entity.Book;
import com.booknest.catalog.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookResource.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ ADD BOOK
    @Test
    void testAddBook() throws Exception {
        Book book = new Book();
        book.setTitle("Java");

        Mockito.when(bookService.addBook(Mockito.any(Book.class)))
                .thenReturn(book);

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"));
    }

    // ✅ GET ALL
    @Test
    void testGetAllBooks() throws Exception {
        Book book = new Book();
        book.setTitle("Spring");

        Mockito.when(bookService.getAllBooks())
                .thenReturn(List.of(book));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring"));
    }

    // ✅ GET BY ID
    @Test
    void testGetBookById() throws Exception {
        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Clean Code");

        Mockito.when(bookService.getBookById(1L))
                .thenReturn(book);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    // ❌ NOT FOUND
    @Test
    void testGetBookById_NotFound() throws Exception {
        Mockito.when(bookService.getBookById(1L))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isInternalServerError());
    }

    // ✅ SEARCH
    @Test
    void testSearchBooks() throws Exception {
        Book book = new Book();
        book.setTitle("Python");

        Mockito.when(bookService.searchBooks("python"))
                .thenReturn(List.of(book));

        mockMvc.perform(get("/books/search")
                .param("keyword", "python"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Python"));
    }

    // ✅ GET BY GENRE
    @Test
    void testGetByGenre() throws Exception {
        Book book = new Book();
        book.setGenre("Tech");

        Mockito.when(bookService.getByGenre("Tech"))
                .thenReturn(List.of(book));

        mockMvc.perform(get("/books/genre/Tech"))
                .andExpect(status().isOk());
    }

    // ✅ UPDATE
    @Test
    void testUpdateBook() throws Exception {
        Book book = new Book();
        book.setTitle("Updated");

        Mockito.when(bookService.updateBook(Mockito.eq(1L), Mockito.any(Book.class)))
                .thenReturn(book);

        mockMvc.perform(put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    // ✅ DELETE
    @Test
    void testDeleteBook() throws Exception {
        Mockito.doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isOk());
    }

    // ✅ UPDATE STOCK
    @Test
    void testUpdateStock() throws Exception {
        Mockito.doNothing().when(bookService).updateStock(1L, 10);

        mockMvc.perform(put("/books/1/stock")
                .param("stock", "10"))
                .andExpect(status().isOk());
    }

    // ✅ FEATURED BOOKS
    @Test
    void testGetFeaturedBooks() throws Exception {
        Book book = new Book();
        book.setRating(4.8);

        Mockito.when(bookService.getFeaturedBooks())
                .thenReturn(List.of(book));

        mockMvc.perform(get("/books/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(4.8));
    }
}