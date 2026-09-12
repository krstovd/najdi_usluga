package com.example.localservices.dto;

import com.example.localservices.entity.ServiceProvider;
import com.example.localservices.entity.VerificationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ProviderResponse(Long id, Long ownerId, Long categoryId, String categoryName, String businessName,
        String description, String address, String city, BigDecimal latitude, BigDecimal longitude, String phone,
        String email, String website, VerificationStatus verificationStatus, BigDecimal averageRating,
        int reviewCount, Instant createdAt, Instant updatedAt) {
    public static ProviderResponse from(ServiceProvider p) {
        return new ProviderResponse(p.getId(), p.getOwner().getId(), p.getCategory().getId(), p.getCategory().getName(),
                p.getBusinessName(), p.getDescription(), p.getAddress(), p.getCity(), p.getLatitude(), p.getLongitude(),
                p.getPhone(), p.getEmail(), p.getWebsite(), p.getVerificationStatus(), p.getAverageRating(),
                p.getReviewCount(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
