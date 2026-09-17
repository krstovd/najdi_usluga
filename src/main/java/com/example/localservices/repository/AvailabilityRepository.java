package com.example.localservices.repository;

import com.example.localservices.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findAllByServiceProviderIdOrderByDayOfWeek(Long providerId);
    Optional<Availability> findByServiceProviderIdAndDayOfWeek(Long providerId, DayOfWeek dayOfWeek);
}
