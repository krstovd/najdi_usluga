package com.example.localservices.dto;

public record AdminStatisticsResponse(long totalUsers, long enabledUsers, long totalProviders,
        long pendingProviders, long verifiedProviders, long totalReservations, long pendingReservations,
        long completedReservations, long totalReviews, long totalCategories) { }
