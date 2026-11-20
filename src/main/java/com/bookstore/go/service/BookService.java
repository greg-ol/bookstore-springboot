package com.bookstore.go.service;

import com.bookstore.go.dto.*;
import com.bookstore.go.exception.BookNotFoundException;
import com.bookstore.go.exception.DuplicatedBookException;
import com.bookstore.go.exception.InvalidBookDataException;
import com.bookstore.go.model.Book;
import com.bookstore.go.model.BookType;
import com.bookstore.go.repository.BookRepository;
import com.bookstore.go.repository.BookSearchCriteria;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Book service - business logic implementation.
 */
@Service
public class BookService {

    private static final int MAX_YEAR_ANTIQUE = 1900;

    private final BookRepository repository;
    private final BookPriceHelper helper;

    public BookService(BookRepository repository, BookPriceHelper helper) {
        this.repository = repository;
        this.helper = helper;
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

    @Transactional(readOnly = true)
    public BookResponseDto getByBarcode(String barcode) {
        Book book = findByBarcode(barcode);
        return convertToResponse(book);
    }

    @Transactional
    public BookResponseDto update(String barcode, BookUpdateRequestDto request) {
        Book book = findByBarcode(barcode);
        if (hasValue(request.name())) book.setName(request.name());
        if (hasValue(request.author())) book.setAuthor(request.author());
        if (request.quantity() != null) book.setQuantity(request.quantity());
        if (request.price() != null) book.setPrice(request.price());
        if (request.releaseYear() != null) book.setReleaseYear(request.releaseYear());
        if (request.scienceIndex() != null) book.setScienceIndex(request.scienceIndex());

        checkValidity(book);
        return convertToResponse(book);
    }

    @Transactional(readOnly = true)
    public TotalPriceResponseDto getTotalPrice(List<String> barcodes) {
        Set<String> requestBarcodes = new HashSet<>(barcodes);

        BigDecimal sum = BigDecimal.valueOf(0);

        for (String bc : requestBarcodes) {
            Book b = findByBarcode(bc);
            BigDecimal price = this.helper.totalPrice(b);
            sum = sum.add(price);
        }

        return new TotalPriceResponseDto(sum);
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<String>> getBarcodesByQuantity() {
        Comparator<Book> compByTotal = Comparator.comparing(helper::totalPrice)
                .thenComparing(Book::getBarcode);

        // all books with quantity bigger then 0
        List<Book> books = repository.findAll()
                .stream().filter(b -> b.getQuantity() > 0).toList();

        return books
                .stream().collect(Collectors.groupingBy(Book::getQuantity, TreeMap::new,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().sorted(compByTotal).map(Book::getBarcode).toList())));
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> search(BookSearchCriteriaDto criteria) {
        return repository.findAll(BookSearchCriteria.getSearchSpec(criteria))
                .stream().sorted(Comparator.comparing(Book::getBarcode))
                .map(this::convertToResponse).toList();
    }

    @Transactional
    public List<BookResponseDto> export() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Book::getBarcode))
                .map(BookResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ImportResultDto importBooks(List<BookRequestDto> books) {
        AtomicInteger createdRecords = new AtomicInteger();
        books.forEach(b -> {
            String barcode = b.barcode();
            Optional<Book> existingBook = repository.findByBarcode(barcode);

            // avoid duplications
            if (existingBook.isPresent()) {
                throw new DuplicatedBookException(barcode);
            }

            Book record = new Book();
            record.setBarcode(barcode);
            populateBook(record, b);
            checkValidity(record);

            repository.save(record);
            createdRecords.getAndIncrement();
        });

        return new ImportResultDto(createdRecords.get());
    }

    private boolean hasValue(String str) {
        return str != null && !str.isBlank();
    }


    private Book findByBarcode(String barcode) {
        return repository.findByBarcode(barcode).orElseThrow(
                () -> new BookNotFoundException("Not found book with barcode: " + barcode));
    }

    private BookResponseDto convertToResponse(Book b) {
        return BookResponseDto.from(b);
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
            report("science index not allowed for Book Type: " + b.getType());
        }
    }


    private static void report(String message) {
        throw new InvalidBookDataException(message);
    }

}
