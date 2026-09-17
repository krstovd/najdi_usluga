package com.example.localservices.service.impl;

import com.example.localservices.dto.CategoryResponse;
import com.example.localservices.repository.CategoryRepository;
import com.example.localservices.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    public CategoryServiceImpl(CategoryRepository repository) { this.repository = repository; }
    @Override @Transactional(readOnly = true)
    public Page<CategoryResponse> listActive(Pageable pageable) { return repository.findAllByActiveTrue(pageable).map(CategoryResponse::from); }
}
