package com.example.localservices.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record SearchCriteria(
        @Size(max = 150) String query,
        @Size(max = 120) String category,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @DecimalMin("0.1") @DecimalMax("50.0") BigDecimal radius,
        @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal minimumRating,
        @Pattern(regexp = "distance|rating|reviews|name", message = "must be distance, rating, reviews, or name") String sort,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size) {

    public SearchCriteria {
        radius = radius == null ? BigDecimal.valueOf(5) : radius;
        sort = sort == null || sort.isBlank() ? (latitude == null ? "rating" : "distance") : sort.toLowerCase();
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
    }

    @AssertTrue(message = "latitude and longitude must be supplied together")
    public boolean isLocationComplete() { return (latitude == null) == (longitude == null); }

    @AssertTrue(message = "distance sorting requires latitude and longitude")
    public boolean isDistanceSortValid() { return !"distance".equals(sort) || latitude != null; }
}
