package com.bookstore.catalogue;

import com.bookstore.catalogue.dto.BookDetailResponse;
import com.bookstore.catalogue.dto.BookSummaryResponse;
import com.bookstore.catalogue.dto.PagedBooksResponse;
import com.bookstore.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only catalogue service: browse, search, filter, and detail (FR-02).
 */
@Service
public class BookService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookRepository bookRepository;
    private final CatalogueMapper mapper;

    public BookService(BookRepository bookRepository, CatalogueMapper mapper) {
        this.bookRepository = bookRepository;
        this.mapper = mapper;
    }

    /**
     * Browse / search / filter books. All filter parameters are optional (null = ignored).
     * Blank strings are normalised to null so empty query params do not over-filter.
     */
    @Transactional(readOnly = true)
    public PagedBooksResponse searchBooks(String title, String authorName,
                                          Long categoryId, Long brandId,
                                          int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("title").ascending());

        Page<Book> result = bookRepository.search(
                blankToNull(title),
                blankToNull(authorName),
                categoryId,
                brandId,
                pageable);

        List<BookSummaryResponse> content = result.getContent()
                .stream()
                .map(mapper::toSummary)
                .toList();

        return new PagedBooksResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public BookDetailResponse getBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        return mapper.toDetail(book);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
