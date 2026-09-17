package com.example.localservices.dto;

import java.time.LocalTime;

public record AvailableSlotResponse(LocalTime startTime, LocalTime endTime) { }
