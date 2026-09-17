package com.example.localservices.dto;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SearchCriteriaValidationTest {
    @Test
    void rejectsIncompleteLocation() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var criteria = new SearchCriteria(null, null, new BigDecimal("41.9"), null,
                    new BigDecimal("5"), null, "rating", 0, 20);
            assertThat(factory.getValidator().validate(criteria))
                    .anyMatch(v -> v.getMessage().contains("supplied together"));
        }
    }

    @Test
    void rejectsDistanceSortWithoutCoordinates() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var criteria = new SearchCriteria(null, null, null, null, null, null, "distance", null, null);
            assertThat(factory.getValidator().validate(criteria))
                    .anyMatch(v -> v.getMessage().contains("requires latitude"));
        }
    }
}
