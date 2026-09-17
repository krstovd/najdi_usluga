package com.example.localservices.controller;

import com.example.localservices.dto.*;
import com.example.localservices.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;
    public ReservationController(ReservationService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    ReservationResponse create(Authentication auth, @Valid @RequestBody ReservationCreateRequest request) { return service.create(auth.getName(), request); }
    @GetMapping("/my") Page<ReservationResponse> mine(Authentication auth, Pageable pageable) { return service.getMine(auth.getName(), pageable); }
    @GetMapping("/provider") Page<ReservationResponse> provider(Authentication auth, Pageable pageable) { return service.getForOwnProvider(auth.getName(), pageable); }
    @GetMapping("/{id}") ReservationResponse get(Authentication auth, @PathVariable Long id) { return service.getAccessible(auth.getName(), id); }
    @PutMapping("/{id}/cancel") ReservationResponse cancel(Authentication auth, @PathVariable Long id) { return service.cancelOwn(auth.getName(), id); }
    @PutMapping("/{id}/confirm") ReservationResponse confirm(Authentication auth, @PathVariable Long id) { return service.confirm(auth.getName(), id); }
    @PutMapping("/{id}/reject") ReservationResponse reject(Authentication auth, @PathVariable Long id) { return service.reject(auth.getName(), id); }
    @PutMapping("/{id}/complete") ReservationResponse complete(Authentication auth, @PathVariable Long id) { return service.complete(auth.getName(), id); }

    @GetMapping("/providers/{providerId}/available-slots")
    List<AvailableSlotResponse> slots(@PathVariable Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getAvailableSlots(providerId, date);
    }
}
