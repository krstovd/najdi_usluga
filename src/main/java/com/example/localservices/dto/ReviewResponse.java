package com.example.localservices.dto;

import com.example.localservices.entity.Review;
import java.time.Instant;

public record ReviewResponse(Long id, Long userId, String reviewerName, Long providerId, String providerName,
        Long reservationId, int rating, String comment, Instant createdAt, Instant updatedAt) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(review.getId(), review.getUser().getId(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName(),
                review.getServiceProvider().getId(), review.getServiceProvider().getBusinessName(),
                review.getReservation().getId(), review.getRating(), review.getComment(),
                review.getCreatedAt(), review.getUpdatedAt());
    }
}
