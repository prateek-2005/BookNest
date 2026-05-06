package com.booknest.catalog.repository;

import com.booknest.catalog.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByGenre(String genre);
    Book findByIsbn(String isbn);
    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);

    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Book> searchAllFields(@Param("kw") String keyword);

    @Modifying
    @Query("UPDATE Book b SET b.stock = b.stock - :quantity WHERE b.bookId = :id AND b.stock >= :quantity")
    int reduceStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);
}

