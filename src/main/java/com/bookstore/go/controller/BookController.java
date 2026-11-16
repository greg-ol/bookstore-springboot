package com.bookstore.go.controller;

import com.bookstore.go.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API controller.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @PostMapping
    // add new

    // get barcode

    // search criteria

    // update

    // calculate total price

    // request list of all barcodes grou by quantity, barcodes sorted by total price
}
