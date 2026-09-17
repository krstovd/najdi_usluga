package com.example.localservices.service;

import com.example.localservices.dto.ReservationCreateRequest;
import com.example.localservices.entity.*;
import com.example.localservices.exception.InvalidReservationStateException;
import com.example.localservices.exception.ReservationConflictException;
import com.example.localservices.repository.*;
import com.example.localservices.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationServiceImplTest {
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ServiceProviderRepository providers = mock(ServiceProviderRepository.class);
    private final AvailabilityRepository availability = mock(AvailabilityRepository.class);
    private final ReservationServiceImpl service = new ReservationServiceImpl(reservations, users, providers, availability);
    private User customer;
    private User owner;
    private ServiceProvider provider;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        reset(reservations, users, providers, availability);
        customer = new User("Mila", "Customer", "mila@example.com", "hash", UserRole.USER);
        owner = new User("Petar", "Provider", "petar@example.com", "hash", UserRole.PROVIDER);
        provider = new ServiceProvider(owner, new Category("Repair", "repair"), "Fix It");
        provider.setVerificationStatus(VerificationStatus.VERIFIED);
        date = LocalDate.now().plusDays(2);
        Availability hours = new Availability(provider, date.getDayOfWeek()); hours.setStartTime(LocalTime.of(8, 0)); hours.setEndTime(LocalTime.of(16, 0));
        when(users.findByEmailIgnoreCase("mila@example.com")).thenReturn(Optional.of(customer));
        when(users.findByEmailIgnoreCase("petar@example.com")).thenReturn(Optional.of(owner));
        when(providers.findByIdAndVerificationStatus(1L, VerificationStatus.VERIFIED)).thenReturn(Optional.of(provider));
        when(providers.findByOwnerId(isNull())).thenReturn(Optional.of(provider));
        when(availability.findByServiceProviderIdAndDayOfWeek(isNull(), eq(date.getDayOfWeek()))).thenReturn(Optional.of(hours));
        when(reservations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsPendingReservationInsideWorkingHours() {
        when(reservations.findConflictingForUpdate(isNull(), eq(date), eq(LocalTime.of(10, 0)), eq(LocalTime.of(10, 30)), anyCollection())).thenReturn(List.of());
        var response = service.create("mila@example.com", request(LocalTime.of(10, 0), LocalTime.of(10, 30)));
        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
        verify(reservations).save(any(Reservation.class));
    }

    @Test
    void rejectsOverlappingReservation() {
        Reservation existing = new Reservation(customer, provider, date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(reservations.findConflictingForUpdate(any(), eq(date), any(), any(), anyCollection())).thenReturn(List.of(existing));
        assertThatThrownBy(() -> service.create("mila@example.com", request(LocalTime.of(10, 30), LocalTime.of(11, 0))))
                .isInstanceOf(ReservationConflictException.class);
        verify(reservations, never()).save(any());
    }

    @Test
    void providerCanOnlyConfirmPendingReservation() {
        Reservation reservation = new Reservation(customer, provider, date, LocalTime.of(10, 0), LocalTime.of(10, 30));
        when(reservations.findById(4L)).thenReturn(Optional.of(reservation));
        assertThat(service.confirm("petar@example.com", 4L).status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThatThrownBy(() -> service.confirm("petar@example.com", 4L)).isInstanceOf(InvalidReservationStateException.class);
    }

    private ReservationCreateRequest request(LocalTime start, LocalTime end) {
        return new ReservationCreateRequest(1L, date, start, end, "Please call first");
    }
}
