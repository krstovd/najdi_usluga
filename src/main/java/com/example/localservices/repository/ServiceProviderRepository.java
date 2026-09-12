package com.example.localservices.repository;

import com.example.localservices.entity.ServiceProvider;
import com.example.localservices.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Optional<ServiceProvider> findByOwnerId(Long ownerId);
    Optional<ServiceProvider> findByIdAndVerificationStatus(Long id, VerificationStatus status);
    Page<ServiceProvider> findAllByVerificationStatus(VerificationStatus status, Pageable pageable);
}
