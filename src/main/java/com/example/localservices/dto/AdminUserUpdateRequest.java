package com.example.localservices.dto;

import com.example.localservices.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateRequest(@NotNull UserRole role, boolean enabled) { }
