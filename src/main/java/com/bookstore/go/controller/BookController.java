package com.bookstore.go.controller;

import com.bookstore.go.dto.*;
import com.bookstore.go.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // different search criteria
    @GetMapping()
    public List<BookResponseDto> search(@RequestBody BookSearchCriteriaDto criteria) {
        return service.search(criteria);
    }


    @PatchMapping("/{barcode}")
    public BookResponseDto update(@PathVariable String barcode, @Valid @RequestBody BookUpdateRequestDto request) {
        return service.update(barcode, request);
    }

    @GetMapping("/total-price")
    public TotalPriceResponseDto getTotalPrice(@RequestParam List<String> barcodes) {
        return service.getTotalPrice(barcodes);
    }

    @GetMapping("/barcodes-by-quantity")
    public Map<Integer, List<String>> getBarcodesByQuanitty() {
        return service.getBarcodesByQuantity();
    }

    @GetMapping("/export")
    public List<BookResponseDto> export() {
        return service.export();
    }

    @PostMapping("/import")
    public ImportResultDto importBooks(@RequestBody List<BookRequestDto> books) {
        return service.importBooks(books);
    }
}