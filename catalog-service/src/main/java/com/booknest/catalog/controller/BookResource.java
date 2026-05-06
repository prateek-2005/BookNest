package com.booknest.catalog.controller;

import com.booknest.catalog.entity.Book;
import com.booknest.catalog.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookResource {
    private static final Logger log = LoggerFactory.getLogger(BookResource.class);

    @Autowired
    private BookService bookService;

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String keyword) {
        return bookService.searchBooks(keyword);
    }

    @GetMapping("/genre/{genre}")
    public List<Book> getByGenre(@PathVariable String genre) {
        return bookService.getByGenre(genre);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @PutMapping("/{id}/stock")
    public void updateStock(@PathVariable Long id, @RequestParam int stock) {
        bookService.updateStock(id, stock);
    }

    @GetMapping("/featured")
    public List<Book> getFeaturedBooks() {
        return bookService.getFeaturedBooks();
    }
}
