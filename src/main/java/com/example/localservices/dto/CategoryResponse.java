package com.example.localservices.dto;

import com.example.localservices.entity.Category;

public record CategoryResponse(Long id, String name, String slug, String description, boolean active) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.isActive());
    }
}
