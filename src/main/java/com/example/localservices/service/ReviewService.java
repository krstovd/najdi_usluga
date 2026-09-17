package com.example.localservices.service;

import com.example.localservices.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    Page<ReviewResponse> getPublicForProvider(Long providerId, Pageable pageable);
    Page<ReviewResponse> getMine(String email, Pageable pageable);
    ReviewResponse create(String email, ReviewCreateRequest request);
    ReviewResponse updateOwn(String email, Long id, ReviewUpdateRequest request);
    void deleteOwn(String email, Long id);
}
