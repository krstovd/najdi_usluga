package com.example.localservices.service;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import com.example.localservices.exception.*;
import com.example.localservices.repository.*;
import com.example.localservices.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceImplTest {
    private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ServiceProviderRepository providers = mock(ServiceProviderRepository.class);
    private final ReviewServiceImpl service = new ReviewServiceImpl(reviews, reservations, users, providers);
    private User customer;
    private ServiceProvider provider;
    private Reservation completed;

    @BeforeEach
    void setUp() {
        reset(reviews, reservations, users, providers);
        customer = new User("Elena", "Test", "elena@example.com", "hash", UserRole.USER); ReflectionTestUtils.setField(customer, "id", 10L);
        User owner = new User("Owner", "Test", "owner@example.com", "hash", UserRole.PROVIDER); ReflectionTestUtils.setField(owner, "id", 20L);
        provider = new ServiceProvider(owner, new Category("Cleaning", "cleaning"), "Clean Home"); ReflectionTestUtils.setField(provider, "id", 30L);
        completed = new Reservation(customer, provider, LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)); ReflectionTestUtils.setField(completed, "id", 40L); completed.setStatus(ReservationStatus.COMPLETED);
        when(users.findByEmailIgnoreCase("elena@example.com")).thenReturn(Optional.of(customer));
        when(reservations.findById(40L)).thenReturn(Optional.of(completed));
        when(providers.findByIdForUpdate(30L)).thenReturn(Optional.of(provider));
        when(reviews.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void completedReservationCanBeReviewedAndRatingIsRecalculated() {
        when(reviews.findByReservationId(40L)).thenReturn(Optional.empty()); when(reviews.countByServiceProviderIdAndModeratedFalse(30L)).thenReturn(2L); when(reviews.averageRatingForProvider(30L)).thenReturn(4.5);
        var response = service.create("elena@example.com", new ReviewCreateRequest(40L, 5, "Excellent"));
        assertThat(response.rating()).isEqualTo(5); assertThat(provider.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.50")); assertThat(provider.getReviewCount()).isEqualTo(2); verify(providers).save(provider);
    }

    @Test
    void pendingReservationCannotBeReviewed() {
        completed.setStatus(ReservationStatus.PENDING);
        assertThatThrownBy(() -> service.create("elena@example.com", new ReviewCreateRequest(40L, 5, null))).isInstanceOf(InvalidReservationStateException.class);
        verify(reviews, never()).saveAndFlush(any());
    }

    @Test
    void duplicateReviewIsRejected() {
        when(reviews.findByReservationId(40L)).thenReturn(Optional.of(new Review(customer, provider, completed, 4)));
        assertThatThrownBy(() -> service.create("elena@example.com", new ReviewCreateRequest(40L, 5, null))).isInstanceOf(DuplicateResourceException.class);
    }
}
