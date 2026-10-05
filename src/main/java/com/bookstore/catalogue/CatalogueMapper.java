package com.bookstore.catalogue;

import com.bookstore.catalogue.dto.BookDetailResponse;
import com.bookstore.catalogue.dto.BookSummaryResponse;
import com.bookstore.catalogue.dto.BrandResponse;
import com.bookstore.catalogue.dto.CategoryResponse;
import org.springframework.stereotype.Component;

/**
 * Hand-written mappers from catalogue JPA entities to response DTOs.
 *
 * Kept separate from the entities to preserve the DTO/entity boundary.
 * A mapping framework is intentionally not used (simplicity over tooling).
 */
@Component
public class CatalogueMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }

    public BrandResponse toBrandResponse(Brand brand) {
        if (brand == null) {
            return null;
        }
        return new BrandResponse(brand.getId(), brand.getName());
    }

    public BookSummaryResponse toSummary(Book book) {
        Brand brand = book.getBrand();
        return new BookSummaryResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getDeliveryEstimateDays(),
                book.getCategory().getId(),
                book.getCategory().getName(),
                brand != null ? brand.getId() : null,
                brand != null ? brand.getName() : null);
    }

    public BookDetailResponse toDetail(Book book) {
        return new BookDetailResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getDescription(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getDeliveryEstimateDays(),
                toCategoryResponse(book.getCategory()),
                toBrandResponse(book.getBrand()));
    }
}
