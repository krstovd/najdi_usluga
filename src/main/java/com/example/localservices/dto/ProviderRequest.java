package com.example.localservices.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProviderRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 160) String businessName,
        @NotBlank @Size(max = 5000) String description,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 100) String city,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Email @Size(max = 191) String email,
        @Size(max = 255) @Pattern(regexp = "^(https?://.*)?$", message = "must be an http or https URL") String website) { }
