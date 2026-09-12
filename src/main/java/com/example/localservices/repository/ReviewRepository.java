package com.example.localservices.repository;

import com.example.localservices.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByServiceProviderId(Long providerId, Pageable pageable);
    Page<Review> findAllByUserId(Long userId, Pageable pageable);
    Optional<Review> findByReservationId(Long reservationId);
}
