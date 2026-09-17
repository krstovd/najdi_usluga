package com.example.localservices.dto;

import com.example.localservices.entity.Reservation;
import com.example.localservices.entity.ReservationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(Long id, Long userId, String customerName, Long providerId, String providerName,
        LocalDate reservationDate, LocalTime startTime, LocalTime endTime, ReservationStatus status, String notes,
        Instant createdAt, Instant updatedAt) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getUser().getId(),
                reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName(),
                reservation.getServiceProvider().getId(), reservation.getServiceProvider().getBusinessName(),
                reservation.getReservationDate(), reservation.getStartTime(), reservation.getEndTime(),
                reservation.getStatus(), reservation.getNotes(), reservation.getCreatedAt(), reservation.getUpdatedAt());
    }
}
