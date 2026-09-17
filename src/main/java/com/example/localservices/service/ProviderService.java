package com.example.localservices.service;

import com.example.localservices.dto.*;
import com.example.localservices.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProviderService {
    ProviderResponse create(String ownerEmail, ProviderRequest request);
    ProviderResponse updateOwn(String ownerEmail, ProviderRequest request);
    ProviderResponse getPublic(Long id);
    ProviderResponse getOwn(String ownerEmail);
    Page<ProviderResponse> listVerified(Pageable pageable);
    List<AvailabilityResponse> getAvailability(Long providerId);
    List<AvailabilityResponse> getOwnAvailability(String ownerEmail);
    List<AvailabilityResponse> replaceOwnAvailability(String ownerEmail, List<AvailabilityRequest> requests);
    ProviderResponse changeVerificationStatus(Long providerId, VerificationStatus target);
}
