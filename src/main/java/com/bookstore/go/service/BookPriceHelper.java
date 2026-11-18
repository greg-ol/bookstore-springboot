package com.bookstore.go.service;

import com.bookstore.go.model.Book;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Provides logic for price calculation.
 */
@Component
public class BookPriceHelper {

    public BigDecimal totalPrice(Book book) {

        BigDecimal stdCalc = book.getPrice().multiply(BigDecimal.valueOf(book.getQuantity()));

        BigDecimal total = switch (book.getType()) {
            case STANDARD -> stdCalc;
            case ANTIQUE -> {
                int age = LocalDate.now().getYear() - book.getReleaseYear();
                yield stdCalc.multiply(BigDecimal.valueOf(age)).divide(BigDecimal.TEN);
            }
            case SCIENCE_JOURNAL -> stdCalc.multiply(BigDecimal.valueOf(book.getScienceIndex()));
        };

        return total.setScale(2, RoundingMode.HALF_UP);
    }


}
