package com.bookstore.go.repository;

import com.bookstore.go.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for db operations on books.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByBarcode(String barcode);
}
