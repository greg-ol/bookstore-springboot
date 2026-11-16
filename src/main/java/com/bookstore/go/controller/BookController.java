package com.bookstore.go.controller;

import com.bookstore.go.dto.BookRequestDto;
import com.bookstore.go.dto.BookResponseDto;
import com.bookstore.go.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public BookResponseDto add(@Valid @RequestBody BookRequestDto request) {
        return service.addNewBook(request);
    }

    @GetMapping("/{barcode}")
    public BookResponseDto getByBarcode(@PathVariable String barcode) {
        return service.getByBarcode(barcode);
    }

    // search criteria

    @PatchMapping("/{barcode}")
    public BookResponseDto update(@PathVariable String barcode, @Valid @RequestBody BookUpdateRequestDto request) {
        return service.update(barcode, request);
    }

    // calculate total price

    // request list of all barcodes group by quantity, barcodes sorted by total price
}
