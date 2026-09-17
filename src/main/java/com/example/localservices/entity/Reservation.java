package com.example.localservices.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;
    @Column(length = 1000)
    private String notes;

    protected Reservation() { }
    public Reservation(User user, ServiceProvider serviceProvider, LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        this.user = user; this.serviceProvider = serviceProvider; this.reservationDate = reservationDate;
        this.startTime = startTime; this.endTime = endTime;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public ServiceProvider getServiceProvider() { return serviceProvider; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
