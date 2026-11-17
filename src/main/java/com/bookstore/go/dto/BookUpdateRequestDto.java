package com.bookstore.go.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Dto for updating book record.
 */
public record BookUpdateRequestDto(
        String name,
        String author,
        @PositiveOrZero
        Integer quantity,
        @DecimalMin("0.00")
        @Digits(integer = 4, fraction = 2)
        BigDecimal price,
        Integer releaseYear,
        @Min(1)
        @Max(10)
        Integer scienceIndex
) {
}
