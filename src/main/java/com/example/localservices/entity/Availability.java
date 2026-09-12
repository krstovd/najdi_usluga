package com.example.localservices.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "availability", uniqueConstraints = @UniqueConstraint(name = "uk_availability_provider_day", columnNames = {"service_provider_id", "day_of_week"}))
public class Availability extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;
    @Enumerated(EnumType.STRING) @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(nullable = false)
    private boolean closed;

    protected Availability() { }
    public Availability(ServiceProvider serviceProvider, DayOfWeek dayOfWeek) { this.serviceProvider = serviceProvider; this.dayOfWeek = dayOfWeek; }
    public Long getId() { return id; }
    public ServiceProvider getServiceProvider() { return serviceProvider; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }
}
