package com.example.localservices.service;

import com.example.localservices.dto.RegisterRequest;
import com.example.localservices.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse getByEmail(String email);
}
