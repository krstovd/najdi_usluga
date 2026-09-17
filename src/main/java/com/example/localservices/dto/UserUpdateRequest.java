package com.example.localservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(@NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName, @Size(max = 30) String phone) { }
