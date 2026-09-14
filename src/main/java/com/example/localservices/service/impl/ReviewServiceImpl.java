package com.example.localservices.service.impl;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import com.example.localservices.exception.*;
import com.example.localservices.repository.*;
import com.example.localservices.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviews;
    private final ReservationRepository reservations;
    private final UserRepository users;
    private final ServiceProviderRepository providers;

    public ReviewServiceImpl(ReviewRepository reviews, ReservationRepository reservations,
            UserRepository users, ServiceProviderRepository providers) {
        this.reviews = reviews; this.reservations = reservations; this.users = users; this.providers = providers;
    }

    @Override public Page<ReviewResponse> getPublicForProvider(Long providerId, Pageable pageable) {
        providers.findByIdAndVerificationStatus(providerId, VerificationStatus.VERIFIED)
                .orElseThrow(() -> new ResourceNotFoundException("Verified provider not found"));
        return reviews.findAllByServiceProviderIdAndModeratedFalse(providerId, pageable).map(ReviewResponse::from);
    }

    @Override public Page<ReviewResponse> getMine(String email, Pageable pageable) {
        return reviews.findAllByUserId(user(email).getId(), pageable).map(ReviewResponse::from);
    }

    @Override @Transactional public ReviewResponse create(String email, ReviewCreateRequest request) {
        User author = user(email); Reservation reservation = reservation(request.reservationId());
        if (!sameUser(author, reservation.getUser())) throw new ForbiddenOperationException("You can only review your own reservation");
        if (reservation.getStatus() != ReservationStatus.COMPLETED) throw new InvalidReservationStateException("A review requires a completed reservation");
        if (reviews.findByReservationId(reservation.getId()).isPresent()) throw new DuplicateResourceException("This reservation already has a review");
        ServiceProvider provider = lockProvider(reservation.getServiceProvider().getId());
        Review review = new Review(author, provider, reservation, request.rating()); review.setComment(normalize(request.comment()));
        Review saved = reviews.saveAndFlush(review); refreshRating(provider); return ReviewResponse.from(saved);
    }

    @Override @Transactional public ReviewResponse updateOwn(String email, Long id, ReviewUpdateRequest request) {
        Review review = review(id); requireOwner(email, review); ServiceProvider provider = lockProvider(review.getServiceProvider().getId());
        review.setRating(request.rating()); review.setComment(normalize(request.comment())); reviews.flush(); refreshRating(provider); return ReviewResponse.from(review);
    }

    @Override @Transactional public void deleteOwn(String email, Long id) {
        Review review = review(id); requireOwner(email, review); ServiceProvider provider = lockProvider(review.getServiceProvider().getId());
        reviews.delete(review); reviews.flush(); refreshRating(provider);
    }

    private void refreshRating(ServiceProvider provider) {
        long count = reviews.countByServiceProviderIdAndModeratedFalse(provider.getId());
        Double average = reviews.averageRatingForProvider(provider.getId());
        provider.setReviewCount(Math.toIntExact(count));
        provider.setAverageRating(average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        providers.save(provider);
    }
    private User user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private Reservation reservation(Long id) { return reservations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found")); }
    private Review review(Long id) { return reviews.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found")); }
    private ServiceProvider lockProvider(Long id) { return providers.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Provider not found")); }
    private void requireOwner(String email, Review review) { if (!sameUser(user(email), review.getUser())) throw new ForbiddenOperationException("You can only change your own reviews"); }
    private boolean sameUser(User left, User right) { return left == right || left.getId() != null && left.getId().equals(right.getId()); }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
