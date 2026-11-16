package com.bookstore.go.dto;

import com.bookstore.go.model.BookType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Dto for passing Book data in request.
 */
public record BookRequestDto(
        @NotBlank
        String name,
        @NotBlank
        String author,
        @NotBlank
        String barcode,
        @NotNull
        @PositiveOrZero
        Integer quantity,
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 4, fraction = 2)
        BigDecimal price,
        BookType type,
        @Max(1900)
        Integer releaseYear,
        @Min(1)
        @Max(10)
        Integer scienceIndex) {
}
