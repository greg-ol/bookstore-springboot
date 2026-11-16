package com.bookstore.go.exception;

/**
 * Book not found exception.
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
