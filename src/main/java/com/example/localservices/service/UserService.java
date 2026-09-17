package com.example.localservices.service;

import com.example.localservices.dto.RegisterRequest;
import com.example.localservices.dto.UserResponse;
import com.example.localservices.dto.UserUpdateRequest;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse getByEmail(String email);
    UserResponse updateOwn(String email, UserUpdateRequest request);
}
