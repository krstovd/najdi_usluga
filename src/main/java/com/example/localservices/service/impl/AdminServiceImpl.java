package com.example.localservices.service.impl;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import com.example.localservices.exception.*;
import com.example.localservices.repository.*;
import com.example.localservices.service.AdminService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    private final UserRepository users; private final ServiceProviderRepository providers; private final CategoryRepository categories;
    private final ReservationRepository reservations; private final ReviewRepository reviews;
    public AdminServiceImpl(UserRepository users, ServiceProviderRepository providers, CategoryRepository categories,
            ReservationRepository reservations, ReviewRepository reviews) { this.users = users; this.providers = providers; this.categories = categories; this.reservations = reservations; this.reviews = reviews; }

    @Override public AdminStatisticsResponse statistics() { return new AdminStatisticsResponse(users.count(), users.countByEnabledTrue(), providers.count(), providers.countByVerificationStatus(VerificationStatus.PENDING), providers.countByVerificationStatus(VerificationStatus.VERIFIED), reservations.count(), reservations.countByStatus(ReservationStatus.PENDING), reservations.countByStatus(ReservationStatus.COMPLETED), reviews.count(), categories.count()); }
    @Override public Page<UserResponse> users(String query, UserRole role, Boolean enabled, Pageable pageable) { return users.adminSearch(like(query), role, enabled, pageable).map(UserResponse::from); }
    @Override @Transactional public UserResponse updateUser(String adminEmail, Long id, AdminUserUpdateRequest request) { User admin = userByEmail(adminEmail); User target = user(id); if (admin.getId().equals(target.getId()) && (!request.enabled() || request.role() != UserRole.ADMIN)) throw new IllegalArgumentException("Administrators cannot disable or remove their own administrator role"); target.setRole(request.role()); target.setEnabled(request.enabled()); return UserResponse.from(target); }
    @Override public Page<ProviderResponse> providers(String query, VerificationStatus status, Pageable pageable) { return providers.adminSearch(like(query), status, pageable).map(ProviderResponse::from); }
    @Override public Page<CategoryResponse> categories(Pageable pageable) { return categories.findAll(pageable).map(CategoryResponse::from); }
    @Override @Transactional public CategoryResponse createCategory(CategoryRequest request) { ensureUniqueCategory(request, null); Category category = new Category(request.name().trim(), request.slug().trim().toLowerCase(Locale.ROOT)); apply(category, request); return CategoryResponse.from(categories.save(category)); }
    @Override @Transactional public CategoryResponse updateCategory(Long id, CategoryRequest request) { Category category = category(id); ensureUniqueCategory(request, category); apply(category, request); return CategoryResponse.from(category); }
    @Override public Page<ReservationResponse> reservations(ReservationStatus status, Pageable pageable) { return reservations.adminSearch(status, pageable).map(ReservationResponse::from); }
    @Override @Transactional public ReservationResponse cancelReservation(Long id) { Reservation reservation = reservation(id); if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.REJECTED) throw new InvalidReservationStateException("This reservation cannot be cancelled"); reservation.setStatus(ReservationStatus.CANCELLED); return ReservationResponse.from(reservation); }
    @Override public Page<AdminReviewResponse> reviews(Boolean moderated, Pageable pageable) { return reviews.adminSearch(moderated, pageable).map(AdminReviewResponse::from); }
    @Override @Transactional public AdminReviewResponse setReviewModerated(Long id, boolean moderated) { Review review = review(id); ServiceProvider provider = providers.findByIdForUpdate(review.getServiceProvider().getId()).orElseThrow(() -> new ResourceNotFoundException("Provider not found")); review.setModerated(moderated); reviews.flush(); refreshRating(provider); return AdminReviewResponse.from(review); }

    private void refreshRating(ServiceProvider provider) { long count = reviews.countByServiceProviderIdAndModeratedFalse(provider.getId()); Double average = reviews.averageRatingForProvider(provider.getId()); provider.setReviewCount(Math.toIntExact(count)); provider.setAverageRating(average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP)); providers.save(provider); }
    private String like(String query) { return query == null || query.isBlank() ? null : "%" + query.trim().toLowerCase(Locale.ROOT) + "%"; }
    private User user(Long id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private User userByEmail(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private Category category(Long id) { return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found")); }
    private Reservation reservation(Long id) { return reservations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found")); }
    private Review review(Long id) { return reviews.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found")); }
    private void apply(Category category, CategoryRequest request) { category.setName(request.name().trim()); category.setSlug(request.slug().trim().toLowerCase(Locale.ROOT)); category.setDescription(request.description() == null || request.description().isBlank() ? null : request.description().trim()); category.setActive(request.active()); }
    private void ensureUniqueCategory(CategoryRequest request, Category existing) { String name = request.name().trim(); String slug = request.slug().trim(); if ((existing == null || !existing.getName().equalsIgnoreCase(name)) && categories.existsByNameIgnoreCase(name)) throw new DuplicateResourceException("Category name already exists"); if ((existing == null || !existing.getSlug().equalsIgnoreCase(slug)) && categories.existsBySlugIgnoreCase(slug)) throw new DuplicateResourceException("Category slug already exists"); }
}
