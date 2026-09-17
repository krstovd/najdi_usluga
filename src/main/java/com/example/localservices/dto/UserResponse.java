package com.example.localservices.dto;

import com.example.localservices.entity.User;
import com.example.localservices.entity.UserRole;
import java.time.Instant;

public record UserResponse(Long id, String firstName, String lastName, String email, String phone,
                           UserRole role, boolean enabled, Instant createdAt, Instant updatedAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPhone(), user.getRole(), user.isEnabled(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
