package com.example.localservices.controller;

import com.example.localservices.dto.CategoryResponse;
import com.example.localservices.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }
    @GetMapping Page<CategoryResponse> list(Pageable pageable) { return service.listActive(pageable); }
}
