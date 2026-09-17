package com.example.localservices.dto;

import com.example.localservices.repository.ProviderSearchProjection;
import java.math.BigDecimal;

public record ProviderSearchResponse(Long id, String businessName, Long categoryId, String categoryName,
        String description, String address, String city, BigDecimal latitude, BigDecimal longitude,
        BigDecimal averageRating, int reviewCount, Double distanceKm) {
    public static ProviderSearchResponse from(ProviderSearchProjection p) {
        Double distance = p.getDistanceKm() == null ? null : Math.round(p.getDistanceKm() * 100.0) / 100.0;
        return new ProviderSearchResponse(p.getId(), p.getBusinessName(), p.getCategoryId(), p.getCategoryName(),
                p.getDescription(), p.getAddress(), p.getCity(), p.getLatitude(), p.getLongitude(),
                p.getAverageRating(), p.getReviewCount(), distance);
    }
}
