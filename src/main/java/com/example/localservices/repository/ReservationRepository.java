package com.example.localservices.repository;

import com.example.localservices.entity.Reservation;
import com.example.localservices.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findAllByUserId(Long userId, Pageable pageable);
    Page<Reservation> findAllByServiceProviderId(Long providerId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r from Reservation r
            where r.serviceProvider.id = :providerId
              and r.reservationDate = :date
              and r.status in :statuses
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    List<Reservation> findConflictingForUpdate(
            @Param("providerId") Long providerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses);
}
