package com.example.localservices.service.impl;

import com.example.localservices.dto.RegisterRequest;
import com.example.localservices.dto.UserResponse;
import com.example.localservices.dto.UserUpdateRequest;
import com.example.localservices.entity.User;
import com.example.localservices.entity.UserRole;
import com.example.localservices.exception.DuplicateResourceException;
import com.example.localservices.exception.ResourceNotFoundException;
import com.example.localservices.repository.UserRepository;
import com.example.localservices.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository; this.passwordEncoder = passwordEncoder;
    }

    @Override @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) throw new DuplicateResourceException("Email is already registered");
        User user = new User(request.firstName().trim(), request.lastName().trim(), email,
                passwordEncoder.encode(request.password()), UserRole.USER);
        user.setPhone(normalizeNullable(request.phone()));
        return UserResponse.from(userRepository.save(user));
    }

    @Override @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override @Transactional
    public UserResponse updateOwn(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(normalizeNullable(request.phone()));
        return UserResponse.from(user);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
