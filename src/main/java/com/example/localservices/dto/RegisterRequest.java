package com.example.localservices.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 191) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 30) String phone) { }
