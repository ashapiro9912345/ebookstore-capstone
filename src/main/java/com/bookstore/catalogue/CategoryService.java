package com.bookstore.catalogue;

import com.bookstore.catalogue.dto.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only service for book categories (FR-02.2).
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CatalogueMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, CatalogueMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(mapper::toCategoryResponse)
                .toList();
    }
}
