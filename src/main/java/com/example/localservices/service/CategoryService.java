package com.example.localservices.service;

import com.example.localservices.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService { Page<CategoryResponse> listActive(Pageable pageable); }
