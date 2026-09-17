package com.example.localservices.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class PageController {
    @GetMapping("/search")
    String search() { return "search"; }

    @GetMapping("/providers/{id}")
    String providerProfile(@PathVariable Long id, Model model) {
        model.addAttribute("providerId", id);
        return "provider-profile";
    }

    @GetMapping("/provider/location")
    String providerLocation() { return "provider-location"; }

    @GetMapping("/my/reservations")
    String myReservations() { return "my-reservations"; }

    @GetMapping("/provider/reservations")
    String providerReservations() { return "provider-reservations"; }

    @GetMapping("/my/reviews")
    String myReviews() { return "my-reviews"; }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    String adminDashboard() { return "admin-dashboard"; }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/register")
    String register() { return "register"; }

    @GetMapping("/my/profile")
    String myProfile() { return "my-profile"; }

    @GetMapping("/provider/dashboard")
    String providerDashboard() { return "provider-dashboard"; }

    @GetMapping("/provider/availability")
    String providerAvailability() { return "provider-availability"; }
}
