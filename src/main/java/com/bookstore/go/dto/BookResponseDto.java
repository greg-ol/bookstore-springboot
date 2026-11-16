package com.bookstore.go.dto;

import com.bookstore.go.model.Book;
import com.bookstore.go.model.BookType;

import java.math.BigDecimal;

/**
 * Dto for transportation of book response
 */
public record BookResponseDto(
        String name,
        String author,
        String barcode,
        int quantity,
        BigDecimal price,
        BookType type,
        Integer releaseYear,
        Integer scienceIndex,
        BigDecimal totalPrice
) {
    public static BookResponseDto from(Book b, BigDecimal totalPrice) {
        return new BookResponseDto(b.getName(), b.getAuthor(), b.getBarcode(), b.getQuantity(), b.getPrice()
                , b.getType(), b.getReleaseYear(), b.getScienceIndex(), totalPrice);
    }
}
