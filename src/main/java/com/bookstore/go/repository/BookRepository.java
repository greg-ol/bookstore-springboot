package com.bookstore.go.repository;

import com.bookstore.go.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository for db operations on books.
 */
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);
}
