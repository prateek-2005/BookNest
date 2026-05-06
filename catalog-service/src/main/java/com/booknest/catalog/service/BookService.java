package com.booknest.catalog.service;

import com.booknest.catalog.entity.Book;
import java.util.List;

public interface BookService {
    Book addBook(Book book);
    List<Book> getAllBooks();
    Book getBookById(Long id);
    List<Book> searchBooks(String keyword);
    List<Book> getByGenre(String genre);
    Book updateBook(Long id, Book book);
    void deleteBook(Long id);
    void updateStock(Long id, int newStock);
    void reduceStock(Long id, int quantity);
    void restoreStock(Long id, int quantity);
    List<Book> getFeaturedBooks();
}
