package com.bookstore.go.service;

import com.bookstore.go.dto.BookRequestDto;
import com.bookstore.go.dto.BookResponseDto;
import com.bookstore.go.exception.BookNotFoundException;
import com.bookstore.go.exception.DuplicatedBookException;
import com.bookstore.go.exception.InvalidBookDataException;
import com.bookstore.go.model.Book;
import com.bookstore.go.model.BookType;
import com.bookstore.go.repository.BookRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Book service - business logic implementation.
 */
@Service
public class BookService {

    private static final int MAX_YEAR_ANTIQUE = 1900;

    private final BookRepository repository;
    private final Validator validator;

    public BookService(BookRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Transactional
    public BookResponseDto addNewBook(@Valid BookRequestDto request) {
        String barcode = request.barcode().trim();

        if (repository.existsByBarcode(barcode)) {
            throw new DuplicatedBookException(barcode);
        }

        Book book = new Book();
        book.setBarcode(barcode);
        populateBook(book, request);
        return convertToResponse(repository.save(book));
    }

    private BookResponseDto convertToResponse(Book b) {
        return BookResponseDto.from(b, new BigDecimal(1));
    }

    private void populateBook(Book book, BookRequestDto dto) {
        book.setName(dto.name());
        book.setAuthor(dto.author());
        book.setQuantity(dto.quantity());
        book.setPrice(dto.price());
        book.setType(dto.type() == null ? BookType.STANDARD : dto.type());
        book.setReleaseYear(dto.releaseYear());
        book.setScienceIndex(dto.scienceIndex());
        checkValidity(book);
    }

    private void checkValidity(Book b) {
        switch (b.getType()) {
            case ANTIQUE -> {
                checkReleaseYaar(b);
                if (b.getReleaseYear() <= 0 || b.getReleaseYear() > MAX_YEAR_ANTIQUE) {
                    report("release year must be between 1 and 1900");
                }
                checkScienceIndex(b);

            }
            case STANDARD -> {
                checkScienceIndex(b);
                if (b.getScienceIndex() != null) {
                    report("science index only required for SCIENCE_JOURNAL");
                }
            }
            case SCIENCE_JOURNAL -> {
                checkReleaseYaar(b);
                if (b.getScienceIndex() == null || b.getScienceIndex() < 1 || b.getScienceIndex() > 10) {
                    report("science index should have values 1-10");
                }
            }
        }
    }

    private static void checkReleaseYaar(Book b) {
        if (b.getReleaseYear() == null && b.getType() == BookType.ANTIQUE) {
            report("release year required for ANTIQUE book");
        }

        if (b.getReleaseYear() != null && b.getType() != BookType.ANTIQUE) {
            {
                report("release year should be provided only for ANTIQUE book");
            }
        }
    }

    private static void checkScienceIndex(Book b) {
        if (b.getScienceIndex() != null) {
            report("science index not allowed for: " + b.getType());
        }
    }


    private static void report(String message) {
        throw new InvalidBookDataException(message);
    }

    public BookResponseDto getByBarcode(String barcode) {
        Book book = repository.findByBarcode(barcode).orElseThrow(
                () -> new BookNotFoundException("Not found book with barcode: " + barcode));
        return convertToResponse(book);
    }
}
