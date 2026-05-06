package com.booknest.catalog.service;

import com.booknest.catalog.entity.Book;
import com.booknest.catalog.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private com.booknest.catalog.client.NotificationClient notificationClient;

    @Override
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        books.forEach(this::enrichBookData);
        return books;
    }

    @Override
    public Book getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        enrichBookData(book);
        return book;
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        List<Book> books = bookRepository.searchAllFields(keyword);
        books.forEach(this::enrichBookData);
        return books;
    }

    @Override
    public List<Book> getByGenre(String genre) {
        List<Book> books = bookRepository.findByGenre(genre);
        books.forEach(this::enrichBookData);
        return books;
    }

    @Override
    public Book updateBook(Long id, Book book) {
        Book existing = getBookById(id);
        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setPrice(book.getPrice());
        existing.setStock(book.getStock());
        existing.setDescription(book.getDescription());
        existing.setCoverImageUrl(book.getCoverImageUrl());
        return bookRepository.save(existing);
    }

    private void enrichBookData(Book book) {
        String currentUrl = book.getCoverImageUrl();
        boolean isPlaceholder = currentUrl == null || currentUrl.isEmpty() || currentUrl.contains("example.com");
        
        if (isPlaceholder) {
            String title = book.getTitle() != null ? book.getTitle().toLowerCase() : "";
            if (title.contains("effective java")) {
                book.setCoverImageUrl("/covers/effective-java.png");
            } else if (title.contains("clean code")) {
                book.setCoverImageUrl("/covers/clean-code.png");
            } else if (title.contains("python")) {
                book.setCoverImageUrl("/covers/python.png");
            }
        }
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStock(Long id, int newStock) {
        Book book = getBookById(id);
        book.setStock(newStock);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void reduceStock(Long id, int quantity) {
        int updated = bookRepository.reduceStockAtomic(id, quantity);
        if (updated == 0) {
            Book book = getBookById(id);
            if (book.getStock() < quantity) {
                throw new RuntimeException("Insufficient stock for book: " + book.getTitle());
            }
            throw new RuntimeException("Failed to reduce stock for book: " + book.getTitle());
        }

        // Trigger low-stock alert if remaining stock < 5
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null && book.getStock() != null && book.getStock() < 5) {
            notificationClient.notifyAdmins(
                "LOW_STOCK_ALERT",
                "Critical Inventory Warning: '" + book.getTitle() + "' (ID: " + book.getBookId() + ") has only " + book.getStock() + " units left."
            );
        }
    }

    @Override
    @Transactional
    public void restoreStock(Long id, int quantity) {
        Book book = getBookById(id);
        book.setStock(book.getStock() + quantity);
        bookRepository.save(book);
    }

    @Override
    public List<Book> getFeaturedBooks() {
        // Example: featured books = top rated
        List<Book> books = bookRepository.findAll().stream()
                .filter(b -> b.getRating() != null && b.getRating() >= 4.5)
                .toList();
        books.forEach(this::enrichBookData);
        return books;
    }
}
