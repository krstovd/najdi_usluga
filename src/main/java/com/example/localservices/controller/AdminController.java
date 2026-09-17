package com.example.localservices.controller;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import com.example.localservices.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping("/statistics") AdminStatisticsResponse statistics() { return service.statistics(); }
    @GetMapping("/users") Page<UserResponse> users(@RequestParam(required = false) String query, @RequestParam(required = false) UserRole role, @RequestParam(required = false) Boolean enabled, Pageable pageable) { return service.users(query, role, enabled, pageable); }
    @PutMapping("/users/{id}") UserResponse updateUser(Authentication auth, @PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) { return service.updateUser(auth.getName(), id, request); }
    @GetMapping("/providers") Page<ProviderResponse> providers(@RequestParam(required = false) String query, @RequestParam(required = false) VerificationStatus status, Pageable pageable) { return service.providers(query, status, pageable); }
    @GetMapping("/categories") Page<CategoryResponse> categories(Pageable pageable) { return service.categories(pageable); }
    @PostMapping("/categories") CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) { return service.createCategory(request); }
    @PutMapping("/categories/{id}") CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return service.updateCategory(id, request); }
    @GetMapping("/reservations") Page<ReservationResponse> reservations(@RequestParam(required = false) ReservationStatus status, Pageable pageable) { return service.reservations(status, pageable); }
    @PutMapping("/reservations/{id}/cancel") ReservationResponse cancelReservation(@PathVariable Long id) { return service.cancelReservation(id); }
    @GetMapping("/reviews") Page<AdminReviewResponse> reviews(@RequestParam(required = false) Boolean moderated, Pageable pageable) { return service.reviews(moderated, pageable); }
    @PutMapping("/reviews/{id}/moderated") AdminReviewResponse moderate(@PathVariable Long id, @RequestParam boolean value) { return service.setReviewModerated(id, value); }
}
