package com.example.localservices.service;

import com.example.localservices.dto.AvailableSlotResponse;
import com.example.localservices.dto.ReservationCreateRequest;
import com.example.localservices.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    ReservationResponse create(String email, ReservationCreateRequest request);
    Page<ReservationResponse> getMine(String email, Pageable pageable);
    Page<ReservationResponse> getForOwnProvider(String email, Pageable pageable);
    ReservationResponse getAccessible(String email, Long id);
    ReservationResponse cancelOwn(String email, Long id);
    ReservationResponse confirm(String email, Long id);
    ReservationResponse reject(String email, Long id);
    ReservationResponse complete(String email, Long id);
    List<AvailableSlotResponse> getAvailableSlots(Long providerId, LocalDate date);
}
