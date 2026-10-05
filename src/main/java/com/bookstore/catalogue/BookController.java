package com.bookstore.catalogue;

import com.bookstore.catalogue.dto.BookDetailResponse;
import com.bookstore.catalogue.dto.PagedBooksResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Book catalogue endpoints (public).
 *
 * Implements:
 *   GET /api/books            — list / search / filter (paginated)
 *   GET /api/books/{bookId}   — single book detail
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<PagedBooksResponse> listBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                bookService.searchBooks(title, authorName, categoryId, brandId, page, size));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailResponse> getBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBook(bookId));
    }
}
