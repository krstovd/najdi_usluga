package com.example.localservices.service;

import com.example.localservices.dto.AdminUserUpdateRequest;
import com.example.localservices.entity.*;
import com.example.localservices.repository.*;
import com.example.localservices.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {
    private final UserRepository users = mock(UserRepository.class); private final ServiceProviderRepository providers = mock(ServiceProviderRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class); private final ReservationRepository reservations = mock(ReservationRepository.class); private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final AdminServiceImpl service = new AdminServiceImpl(users, providers, categories, reservations, reviews);

    @BeforeEach void resetMocks() { reset(users, providers, categories, reservations, reviews); }

    @Test void statisticsUseRepositoryCounts() {
        when(users.count()).thenReturn(10L); when(users.countByEnabledTrue()).thenReturn(9L); when(providers.count()).thenReturn(4L); when(providers.countByVerificationStatus(VerificationStatus.PENDING)).thenReturn(1L); when(providers.countByVerificationStatus(VerificationStatus.VERIFIED)).thenReturn(3L); when(reservations.count()).thenReturn(20L); when(reservations.countByStatus(ReservationStatus.PENDING)).thenReturn(2L); when(reservations.countByStatus(ReservationStatus.COMPLETED)).thenReturn(15L); when(reviews.count()).thenReturn(8L); when(categories.count()).thenReturn(5L);
        var result = service.statistics(); assertThat(result.totalUsers()).isEqualTo(10); assertThat(result.verifiedProviders()).isEqualTo(3); assertThat(result.totalReservations()).isEqualTo(20);
    }

    @Test void administratorCannotDisableSelf() {
        User admin = new User("Admin", "User", "admin@example.com", "hash", UserRole.ADMIN); ReflectionTestUtils.setField(admin, "id", 1L); when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin)); when(users.findById(1L)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> service.updateUser("admin@example.com", 1L, new AdminUserUpdateRequest(UserRole.ADMIN, false))).isInstanceOf(IllegalArgumentException.class);
    }
}
