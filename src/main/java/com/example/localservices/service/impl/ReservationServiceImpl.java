package com.example.localservices.service.impl;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import com.example.localservices.exception.*;
import com.example.localservices.repository.*;
import com.example.localservices.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    private static final Set<ReservationStatus> BLOCKING = EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
    private static final Duration SLOT_DURATION = Duration.ofMinutes(30);
    private final ReservationRepository reservations;
    private final UserRepository users;
    private final ServiceProviderRepository providers;
    private final AvailabilityRepository availability;

    public ReservationServiceImpl(ReservationRepository reservations, UserRepository users,
            ServiceProviderRepository providers, AvailabilityRepository availability) {
        this.reservations = reservations; this.users = users; this.providers = providers; this.availability = availability;
    }

    @Override @Transactional
    public ReservationResponse create(String email, ReservationCreateRequest request) {
        User user = user(email);
        if (user.getRole() != UserRole.USER) throw new ForbiddenOperationException("Only customer accounts can create reservations");
        ServiceProvider provider = verifiedProvider(request.providerId());
        validateTime(request.reservationDate(), request.startTime(), request.endTime());
        ensureWithinAvailability(provider.getId(), request.reservationDate(), request.startTime(), request.endTime());
        if (!reservations.findConflictingForUpdate(provider.getId(), request.reservationDate(), request.startTime(), request.endTime(), BLOCKING).isEmpty())
            throw new ReservationConflictException("The selected time overlaps another reservation");
        Reservation reservation = new Reservation(user, provider, request.reservationDate(), request.startTime(), request.endTime());
        reservation.setNotes(normalizeNotes(request.notes()));
        return ReservationResponse.from(reservations.save(reservation));
    }

    @Override public Page<ReservationResponse> getMine(String email, Pageable pageable) {
        return reservations.findAllByUserId(user(email).getId(), pageable).map(ReservationResponse::from);
    }

    @Override public Page<ReservationResponse> getForOwnProvider(String email, Pageable pageable) {
        return reservations.findAllByServiceProviderId(ownProvider(email).getId(), pageable).map(ReservationResponse::from);
    }

    @Override public ReservationResponse getAccessible(String email, Long id) {
        User actor = user(email); Reservation reservation = reservation(id);
        boolean customer = sameUser(reservation.getUser(), actor);
        boolean providerOwner = sameUser(reservation.getServiceProvider().getOwner(), actor);
        if (!customer && !providerOwner && actor.getRole() != UserRole.ADMIN) throw new ForbiddenOperationException("You cannot view this reservation");
        return ReservationResponse.from(reservation);
    }

    @Override @Transactional public ReservationResponse cancelOwn(String email, Long id) {
        User actor = user(email); Reservation reservation = reservation(id);
        if (!sameUser(reservation.getUser(), actor)) throw new ForbiddenOperationException("You can only cancel your own reservations");
        requireState(reservation, ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
        if (!LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime()).isAfter(LocalDateTime.now()))
            throw new InvalidReservationStateException("Past reservations cannot be cancelled");
        reservation.setStatus(ReservationStatus.CANCELLED); return ReservationResponse.from(reservation);
    }

    @Override @Transactional public ReservationResponse confirm(String email, Long id) {
        Reservation reservation = ownProviderReservation(email, id); requireState(reservation, ReservationStatus.PENDING);
        reservation.setStatus(ReservationStatus.CONFIRMED); return ReservationResponse.from(reservation);
    }

    @Override @Transactional public ReservationResponse reject(String email, Long id) {
        Reservation reservation = ownProviderReservation(email, id); requireState(reservation, ReservationStatus.PENDING);
        reservation.setStatus(ReservationStatus.REJECTED); return ReservationResponse.from(reservation);
    }

    @Override @Transactional public ReservationResponse complete(String email, Long id) {
        Reservation reservation = ownProviderReservation(email, id); requireState(reservation, ReservationStatus.CONFIRMED);
        if (LocalDateTime.of(reservation.getReservationDate(), reservation.getEndTime()).isAfter(LocalDateTime.now()))
            throw new InvalidReservationStateException("A reservation can only be completed after its end time");
        reservation.setStatus(ReservationStatus.COMPLETED); return ReservationResponse.from(reservation);
    }

    @Override public List<AvailableSlotResponse> getAvailableSlots(Long providerId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date cannot be in the past");
        ServiceProvider provider = verifiedProvider(providerId);
        Availability schedule = availability.findByServiceProviderIdAndDayOfWeek(provider.getId(), date.getDayOfWeek())
                .orElse(null);
        if (schedule == null || schedule.isClosed()) return List.of();
        List<Reservation> booked = reservations.findAllByServiceProviderIdAndReservationDateAndStatusInOrderByStartTime(providerId, date, BLOCKING);
        List<AvailableSlotResponse> slots = new ArrayList<>();
        for (LocalTime start = schedule.getStartTime(); !start.plus(SLOT_DURATION).isAfter(schedule.getEndTime()); start = start.plus(SLOT_DURATION)) {
            LocalTime slotStart = start;
            LocalTime slotEnd = slotStart.plus(SLOT_DURATION);
            if (date.equals(LocalDate.now()) && !LocalDateTime.of(date, slotStart).isAfter(LocalDateTime.now())) continue;
            boolean conflict = booked.stream().anyMatch(r -> r.getStartTime().isBefore(slotEnd) && r.getEndTime().isAfter(slotStart));
            if (!conflict) slots.add(new AvailableSlotResponse(slotStart, slotEnd));
        }
        return slots;
    }

    private User user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private ServiceProvider verifiedProvider(Long id) { return providers.findByIdAndVerificationStatus(id, VerificationStatus.VERIFIED).orElseThrow(() -> new ResourceNotFoundException("Verified provider not found")); }
    private ServiceProvider ownProvider(String email) { return providers.findByOwnerId(user(email).getId()).orElseThrow(() -> new ResourceNotFoundException("Provider profile not found")); }
    private Reservation reservation(Long id) { return reservations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found")); }
    private Reservation ownProviderReservation(String email, Long id) { Reservation r = reservation(id); if (!sameUser(r.getServiceProvider().getOwner(), user(email))) throw new ForbiddenOperationException("You can only manage reservations for your own provider profile"); return r; }
    private boolean sameUser(User left, User right) { return left == right || left.getId() != null && left.getId().equals(right.getId()); }
    private void requireState(Reservation reservation, ReservationStatus... allowed) { if (!Arrays.asList(allowed).contains(reservation.getStatus())) throw new InvalidReservationStateException("Reservation cannot transition from " + reservation.getStatus()); }
    private void validateTime(LocalDate date, LocalTime start, LocalTime end) { if (!start.isBefore(end)) throw new IllegalArgumentException("Start time must be before end time"); if (!LocalDateTime.of(date, start).isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Reservation must be in the future"); }
    private void ensureWithinAvailability(Long providerId, LocalDate date, LocalTime start, LocalTime end) { Availability schedule = availability.findByServiceProviderIdAndDayOfWeek(providerId, date.getDayOfWeek()).orElseThrow(() -> new ReservationConflictException("Provider is not available on this day")); if (schedule.isClosed() || start.isBefore(schedule.getStartTime()) || end.isAfter(schedule.getEndTime())) throw new ReservationConflictException("Reservation is outside provider working hours"); }
    private String normalizeNotes(String notes) { return notes == null || notes.isBlank() ? null : notes.trim(); }
}
