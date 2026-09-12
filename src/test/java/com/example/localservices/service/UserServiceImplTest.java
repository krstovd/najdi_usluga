package com.example.localservices.service;

import com.example.localservices.dto.RegisterRequest;
import com.example.localservices.dto.UserResponse;
import com.example.localservices.entity.User;
import com.example.localservices.exception.DuplicateResourceException;
import com.example.localservices.repository.UserRepository;
import com.example.localservices.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserServiceImpl service = new UserServiceImpl(repository, encoder);

    @Test
    void registrationNormalizesEmailAndHashesPassword() {
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserResponse response = service.register(new RegisterRequest(" Ana ", " Test ", " ANA@Example.COM ", "secret123", " "));
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(encoder.matches("secret123", saved.getPasswordHash())).isTrue();
        assertThat(saved.getPhone()).isNull();
    }

    @Test
    void duplicateEmailIsRejected() {
        when(repository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.register(new RegisterRequest("Ana", "Test", "ana@example.com", "secret123", null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }
}
