package com.example.localservices.controller;

import com.example.localservices.dto.*;
import com.example.localservices.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/providers")
public class ProviderController {
    private final ProviderService service;
    public ProviderController(ProviderService service) { this.service = service; }

    @GetMapping Page<ProviderResponse> list(Pageable pageable) { return service.listVerified(pageable); }
    @GetMapping("/{id}") ProviderResponse get(@PathVariable Long id) { return service.getPublic(id); }
    @GetMapping("/{id}/availability") List<AvailabilityResponse> availability(@PathVariable Long id) { return service.getAvailability(id); }
    @GetMapping("/me") ProviderResponse own(Authentication auth) { return service.getOwn(auth.getName()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    ProviderResponse create(Authentication auth, @Valid @RequestBody ProviderRequest request) { return service.create(auth.getName(), request); }
    @PutMapping("/me") ProviderResponse update(Authentication auth, @Valid @RequestBody ProviderRequest request) {
        return service.updateOwn(auth.getName(), request);
    }
    @PutMapping("/me/availability")
    List<AvailabilityResponse> replaceAvailability(Authentication auth, @Valid @RequestBody List<@Valid AvailabilityRequest> requests) {
        return service.replaceOwnAvailability(auth.getName(), requests);
    }
}
