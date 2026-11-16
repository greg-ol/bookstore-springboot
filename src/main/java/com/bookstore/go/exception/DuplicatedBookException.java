package com.bookstore.go.exception;

/**
 * Exception for duplicated books (barcode has to be unique).
 */
public class DuplicatedBookException extends RuntimeException {

    public DuplicatedBookException(String barcode) {
        super("Book with barcode: " + barcode + " already exists");
    }
}
