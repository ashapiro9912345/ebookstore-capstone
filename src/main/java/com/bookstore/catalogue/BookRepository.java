package com.bookstore.catalogue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Search/filter books.  All parameters are optional — null values are ignored.
     * Supports case-insensitive partial matching on title and author (FR-02.6).
     */
    @Query("""
            SELECT b FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:authorName IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :authorName, '%')))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:brandId IS NULL OR b.brand.id = :brandId)
            """)
    Page<Book> search(
            @Param("title") String title,
            @Param("authorName") String authorName,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            Pageable pageable
    );
}
