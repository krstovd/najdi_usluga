package com.example.localservices.service;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminStatisticsResponse statistics();
    Page<UserResponse> users(String query, UserRole role, Boolean enabled, Pageable pageable);
    UserResponse updateUser(String adminEmail, Long id, AdminUserUpdateRequest request);
    Page<ProviderResponse> providers(String query, VerificationStatus status, Pageable pageable);
    Page<CategoryResponse> categories(Pageable pageable);
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    Page<ReservationResponse> reservations(ReservationStatus status, Pageable pageable);
    ReservationResponse cancelReservation(Long id);
    Page<AdminReviewResponse> reviews(Boolean moderated, Pageable pageable);
    AdminReviewResponse setReviewModerated(Long id, boolean moderated);
}
