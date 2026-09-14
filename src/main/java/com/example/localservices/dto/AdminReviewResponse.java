package com.example.localservices.dto;

import com.example.localservices.entity.Review;
import java.time.Instant;

public record AdminReviewResponse(Long id, Long userId, String reviewerName, Long providerId,
        String providerName, Long reservationId, int rating, String comment, boolean moderated, Instant createdAt) {
    public static AdminReviewResponse from(Review review) {
        return new AdminReviewResponse(review.getId(), review.getUser().getId(), review.getUser().getFirstName() + " " + review.getUser().getLastName(),
                review.getServiceProvider().getId(), review.getServiceProvider().getBusinessName(), review.getReservation().getId(),
                review.getRating(), review.getComment(), review.isModerated(), review.getCreatedAt());
    }
}
