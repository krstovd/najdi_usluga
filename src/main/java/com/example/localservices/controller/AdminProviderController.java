package com.example.localservices.controller;

import com.example.localservices.dto.ProviderResponse;
import com.example.localservices.entity.VerificationStatus;
import com.example.localservices.service.ProviderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/admin/providers") @PreAuthorize("hasRole('ADMIN')")
public class AdminProviderController {
    private final ProviderService service;
    public AdminProviderController(ProviderService service) { this.service = service; }
    @PutMapping("/{id}/verify") ProviderResponse verify(@PathVariable Long id) { return service.changeVerificationStatus(id, VerificationStatus.VERIFIED); }
    @PutMapping("/{id}/reject") ProviderResponse reject(@PathVariable Long id) { return service.changeVerificationStatus(id, VerificationStatus.REJECTED); }
    @PutMapping("/{id}/suspend") ProviderResponse suspend(@PathVariable Long id) { return service.changeVerificationStatus(id, VerificationStatus.SUSPENDED); }
    @PutMapping("/{id}/reactivate") ProviderResponse reactivate(@PathVariable Long id) { return service.changeVerificationStatus(id, VerificationStatus.VERIFIED); }
}
