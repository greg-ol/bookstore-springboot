package com.bookstore.go.dto;

import java.math.BigDecimal;

/**
 * Dto providing response for total price calculation call.
 */
public record TotalPriceResponseDto(
        BigDecimal totalPrice
) {
}
