package com.example.localservices.repository;

import java.math.BigDecimal;

public interface ProviderSearchProjection {
    Long getId();
    String getBusinessName();
    Long getCategoryId();
    String getCategoryName();
    String getDescription();
    String getAddress();
    String getCity();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    BigDecimal getAverageRating();
    Integer getReviewCount();
    Double getDistanceKm();
}
