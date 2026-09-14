package com.example.localservices.controller;

import com.example.localservices.dto.*;
import com.example.localservices.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService service;
    public ReviewController(ReviewService service) { this.service = service; }

    @GetMapping("/providers/{providerId}") Page<ReviewResponse> provider(@PathVariable Long providerId, Pageable pageable) { return service.getPublicForProvider(providerId, pageable); }
    @GetMapping("/my") Page<ReviewResponse> mine(Authentication auth, Pageable pageable) { return service.getMine(auth.getName(), pageable); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) ReviewResponse create(Authentication auth, @Valid @RequestBody ReviewCreateRequest request) { return service.create(auth.getName(), request); }
    @PutMapping("/{id}") ReviewResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody ReviewUpdateRequest request) { return service.updateOwn(auth.getName(), id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(Authentication auth, @PathVariable Long id) { service.deleteOwn(auth.getName(), id); }
}
