package com.example.localservices.dto;

import com.example.localservices.entity.Availability;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, boolean closed) {
    public static AvailabilityResponse from(Availability a) {
        return new AvailabilityResponse(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.isClosed());
    }
}
