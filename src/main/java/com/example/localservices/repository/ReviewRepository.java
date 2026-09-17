package com.example.localservices.repository;

import com.example.localservices.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByServiceProviderIdAndModeratedFalse(Long providerId, Pageable pageable);
    Page<Review> findAllByUserId(Long userId, Pageable pageable);
    Optional<Review> findByReservationId(Long reservationId);
    long countByServiceProviderIdAndModeratedFalse(Long providerId);

    @org.springframework.data.jpa.repository.Query("select avg(r.rating) from Review r where r.serviceProvider.id = :providerId and r.moderated = false")
    Double averageRatingForProvider(@org.springframework.data.repository.query.Param("providerId") Long providerId);
    @Query("select r from Review r where (:moderated is null or r.moderated = :moderated)")
    Page<Review> adminSearch(@Param("moderated") Boolean moderated, Pageable pageable);
}
