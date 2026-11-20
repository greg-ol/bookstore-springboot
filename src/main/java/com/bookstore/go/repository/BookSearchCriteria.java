package com.bookstore.go.repository;

import com.bookstore.go.dto.BookSearchCriteriaDto;
import com.bookstore.go.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides search specifications.
 */
public final class BookSearchCriteria {

    private BookSearchCriteria() {

    }

    public static Specification<Book> getSearchSpec(BookSearchCriteriaDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(dto.barcode())) {
                predicates.add(cb.equal(root.get("barcode"), dto.barcode()));
            }
            if (StringUtils.hasText(dto.name())) {
                predicates.add(cb.like(cb.lower(root.get("name")), writeContains(dto.name()), '\\'));
            }
            if (StringUtils.hasText(dto.author())) {
                predicates.add(cb.like(cb.lower(root.get("author")), writeContains(dto.author()), '\\'));
            }
            if (dto.quantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), dto.quantity()));
            }
            if (dto.price() != null) {
                predicates.add(cb.equal(root.get("price"), dto.price()));
            }
            if (dto.type() != null) {
                predicates.add(cb.equal(root.get("type"), dto.type()));
            }
            if (dto.releaseYear() != null) {
                predicates.add(cb.equal(root.get("releaseYear"), dto.releaseYear()));
            }
            if (dto.scienceIndex() != null) {
                predicates.add(cb.equal(root.get("scienceIdex"), dto.scienceIndex()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String writeContains(String str) {
        String escapedStr =
                str.toLowerCase().replace("\\", "\\\\").replace("%", "\\%")
                        .replace("_", "\\_");
        return "%" + escapedStr + "%";
    }

}
