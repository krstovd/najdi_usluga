package com.example.localservices.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityRequest(@NotNull DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, boolean closed) { }
