package com.bookstore.go.exception;

/**
 * Exception for invalid book data when creating or updating book.
 */
public class InvalidBookDataException extends RuntimeException{
    public InvalidBookDataException(String message) {
        super(message);
    }
}
