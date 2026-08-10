package com.minhhao.novelscout.catalog.dto;

import com.minhhao.novelscout.catalog.Category;

public record CategoryResponse(Long id, String name, String slug) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
    }
}
