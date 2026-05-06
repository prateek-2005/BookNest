package com.booknest.catalog;

import com.booknest.catalog.entity.Book;
import com.booknest.catalog.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void testSaveAndFindByGenre() {
        Book book = new Book();
        book.setTitle("Spring in Action");
        book.setGenre("Tech");
        book.setPrice(500.0);
        book.setStock(10);
        
        bookRepository.save(book);

        List<Book> found = bookRepository.findByGenre("Tech");
        assertFalse(found.isEmpty());
        assertEquals("Spring in Action", found.get(0).getTitle());
    }
}
