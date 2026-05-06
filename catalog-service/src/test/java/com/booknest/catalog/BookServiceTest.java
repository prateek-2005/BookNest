package com.booknest.catalog;

import com.booknest.catalog.client.NotificationClient;
import com.booknest.catalog.entity.Book;
import com.booknest.catalog.repository.BookRepository;
import com.booknest.catalog.service.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private BookServiceImpl bookService;

    // ✅ ADD BOOK
    @Test
    void testAddBook() {
        Book book = new Book();
        book.setTitle("Java");

        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.addBook(book);

        assertEquals("Java", result.getTitle());
        verify(bookRepository).save(book);
    }

    // ✅ GET ALL (with enrichment)
    @Test
    void testGetAllBooks_enrichment() {
        Book book = new Book();
        book.setTitle("Effective Java");
        book.setCoverImageUrl(null);

        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.getAllBooks();

        assertTrue(result.get(0).getCoverImageUrl().contains("effective-java"));
    }

    // ✅ GET BY ID (success)
    @Test
    void testGetBookById_success() {
        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Clean Code");
        book.setCoverImageUrl("");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertEquals("Clean Code", result.getTitle());
        assertTrue(result.getCoverImageUrl().contains("clean-code"));
    }

    // ❌ GET BY ID (not found)
    @Test
    void testGetBookById_notFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> bookService.getBookById(1L));
    }

    // ✅ SEARCH BOOKS
    @Test
    void testSearchBooks() {
        Book book = new Book();
        book.setTitle("Python Basics");
        book.setCoverImageUrl("");

        when(bookRepository.searchAllFields("python"))
                .thenReturn(List.of(book));

        List<Book> result = bookService.searchBooks("python");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getCoverImageUrl().contains("python"));
    }

    // ✅ GET BY GENRE
    @Test
    void testGetByGenre() {
        Book book = new Book();
        book.setGenre("Tech");

        when(bookRepository.findByGenre("Tech"))
                .thenReturn(List.of(book));

        List<Book> result = bookService.getByGenre("Tech");

        assertEquals(1, result.size());
    }

    // ✅ UPDATE BOOK
    @Test
    void testUpdateBook() {
        Book existing = new Book();
        existing.setBookId(1L);
        existing.setTitle("Old");

        Book updated = new Book();
        updated.setTitle("New");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("New", result.getTitle());
    }

    // ✅ DELETE
    @Test
    void testDeleteBook() {
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    // ✅ UPDATE STOCK
    @Test
    void testUpdateStock() {
        Book book = new Book();
        book.setBookId(1L);
        book.setStock(5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        bookService.updateStock(1L, 10);

        assertEquals(10, book.getStock());
    }

    // ✅ REDUCE STOCK SUCCESS
    @Test
    void testReduceStock_success() {
        when(bookRepository.reduceStockAtomic(1L, 2)).thenReturn(1);

        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Java");
        book.setStock(10);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.reduceStock(1L, 2);

        verify(bookRepository).reduceStockAtomic(1L, 2);
    }

    // ❌ REDUCE STOCK - insufficient
    @Test
    void testReduceStock_insufficient() {
        when(bookRepository.reduceStockAtomic(1L, 10)).thenReturn(0);

        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Java");
        book.setStock(5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookService.reduceStock(1L, 10));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    // ❌ REDUCE STOCK - generic failure
    @Test
    void testReduceStock_failure() {
        when(bookRepository.reduceStockAtomic(1L, 2)).thenReturn(0);

        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Java");
        book.setStock(5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookService.reduceStock(1L, 2));

        assertTrue(ex.getMessage().contains("Failed to reduce stock"));
    }

    // ✅ LOW STOCK NOTIFICATION
    @Test
    void testReduceStock_lowStockNotification() {
        when(bookRepository.reduceStockAtomic(1L, 1)).thenReturn(1);

        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Spring");
        book.setStock(3); // <5 triggers alert

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.reduceStock(1L, 1);

        verify(notificationClient).notifyAdmins(eq("LOW_STOCK_ALERT"), anyString());
    }

    // ✅ RESTORE STOCK
    @Test
    void testRestoreStock() {
        Book book = new Book();
        book.setBookId(1L);
        book.setStock(5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        bookService.restoreStock(1L, 3);

        assertEquals(8, book.getStock());
    }

    // ✅ FEATURED BOOKS
    @Test
    void testGetFeaturedBooks() {
        Book b1 = new Book();
        b1.setRating(4.7);

        Book b2 = new Book();
        b2.setRating(3.0);

        when(bookRepository.findAll()).thenReturn(List.of(b1, b2));

        List<Book> result = bookService.getFeaturedBooks();

        assertEquals(1, result.size());
    }
}