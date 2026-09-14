package com.example.localservices.service.impl;

import com.example.localservices.dto.*;
import com.example.localservices.entity.*;
import com.example.localservices.exception.DuplicateResourceException;
import com.example.localservices.exception.ResourceNotFoundException;
import com.example.localservices.repository.*;
import com.example.localservices.service.ProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.util.*;

@Service
public class ProviderServiceImpl implements ProviderService {
    private final ServiceProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AvailabilityRepository availabilityRepository;

    public ProviderServiceImpl(ServiceProviderRepository providerRepository, UserRepository userRepository,
            CategoryRepository categoryRepository, AvailabilityRepository availabilityRepository) {
        this.providerRepository = providerRepository; this.userRepository = userRepository;
        this.categoryRepository = categoryRepository; this.availabilityRepository = availabilityRepository;
    }

    @Override @Transactional
    public ProviderResponse create(String ownerEmail, ProviderRequest request) {
        User owner = requireUser(ownerEmail);
        if (providerRepository.findByOwnerId(owner.getId()).isPresent())
            throw new DuplicateResourceException("This account already has a provider profile");
        Category category = requireActiveCategory(request.categoryId());
        ServiceProvider provider = new ServiceProvider(owner, category, request.businessName().trim());
        apply(provider, category, request);
        provider.setVerificationStatus(VerificationStatus.PENDING);
        owner.setRole(UserRole.PROVIDER);
        return ProviderResponse.from(providerRepository.save(provider));
    }

    @Override @Transactional
    public ProviderResponse updateOwn(String ownerEmail, ProviderRequest request) {
        ServiceProvider provider = requireOwnedProvider(ownerEmail);
        apply(provider, requireActiveCategory(request.categoryId()), request);
        if (provider.getVerificationStatus() != VerificationStatus.SUSPENDED)
            provider.setVerificationStatus(VerificationStatus.PENDING);
        return ProviderResponse.from(provider);
    }

    @Override @Transactional(readOnly = true)
    public ProviderResponse getPublic(Long id) {
        return ProviderResponse.from(providerRepository.findByIdAndVerificationStatus(id, VerificationStatus.VERIFIED)
                .orElseThrow(() -> new ResourceNotFoundException("Verified provider not found")));
    }

    @Override @Transactional(readOnly = true)
    public ProviderResponse getOwn(String ownerEmail) { return ProviderResponse.from(requireOwnedProvider(ownerEmail)); }

    @Override @Transactional(readOnly = true)
    public Page<ProviderResponse> listVerified(Pageable pageable) {
        return providerRepository.findAllByVerificationStatus(VerificationStatus.VERIFIED, pageable).map(ProviderResponse::from);
    }

    @Override @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailability(Long providerId) {
        if (providerRepository.findByIdAndVerificationStatus(providerId, VerificationStatus.VERIFIED).isEmpty())
            throw new ResourceNotFoundException("Verified provider not found");
        return availabilityRepository.findAllByServiceProviderIdOrderByDayOfWeek(providerId).stream().map(AvailabilityResponse::from).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<AvailabilityResponse> getOwnAvailability(String ownerEmail) {
        ServiceProvider provider = requireOwnedProvider(ownerEmail);
        return availabilityRepository.findAllByServiceProviderIdOrderByDayOfWeek(provider.getId()).stream()
                .map(AvailabilityResponse::from).toList();
    }

    @Override @Transactional
    public List<AvailabilityResponse> replaceOwnAvailability(String ownerEmail, List<AvailabilityRequest> requests) {
        ServiceProvider provider = requireOwnedProvider(ownerEmail);
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (AvailabilityRequest request : requests) {
            if (!days.add(request.dayOfWeek())) throw new IllegalArgumentException("Each day may appear only once");
            validateHours(request);
            Availability availability = availabilityRepository
                    .findByServiceProviderIdAndDayOfWeek(provider.getId(), request.dayOfWeek())
                    .orElseGet(() -> new Availability(provider, request.dayOfWeek()));
            availability.setClosed(request.closed());
            availability.setStartTime(request.closed() ? null : request.startTime());
            availability.setEndTime(request.closed() ? null : request.endTime());
            availabilityRepository.save(availability);
        }
        return availabilityRepository.findAllByServiceProviderIdOrderByDayOfWeek(provider.getId()).stream()
                .map(AvailabilityResponse::from).toList();
    }

    @Override @Transactional
    public ProviderResponse changeVerificationStatus(Long providerId, VerificationStatus target) {
        ServiceProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        if (target == VerificationStatus.PENDING) throw new IllegalArgumentException("Administrators cannot set PENDING directly");
        provider.setVerificationStatus(target);
        return ProviderResponse.from(provider);
    }

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    private ServiceProvider requireOwnedProvider(String email) {
        User owner = requireUser(email);
        return providerRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));
    }
    private Category requireActiveCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .filter(Category::isActive).orElseThrow(() -> new ResourceNotFoundException("Active category not found"));
        return category;
    }
    private void validateHours(AvailabilityRequest request) {
        if (request.closed()) {
            if (request.startTime() != null || request.endTime() != null)
                throw new IllegalArgumentException("Closed days cannot contain working hours");
        } else if (request.startTime() == null || request.endTime() == null || !request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("Open days require a valid start time before end time");
        }
    }
    private void apply(ServiceProvider provider, Category category, ProviderRequest request) {
        provider.setCategory(category); provider.setBusinessName(request.businessName().trim());
        provider.setDescription(request.description().trim()); provider.setAddress(request.address().trim());
        provider.setCity(request.city().trim()); provider.setLatitude(request.latitude()); provider.setLongitude(request.longitude());
        provider.setPhone(request.phone().trim()); provider.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        provider.setWebsite(request.website() == null || request.website().isBlank() ? null : request.website().trim());
    }
}
