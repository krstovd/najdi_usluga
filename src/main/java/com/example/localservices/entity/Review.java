package com.example.localservices.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int rating;
    @Column(length = 2000)
    private String comment;
    @Column(nullable = false)
    private boolean moderated;

    protected Review() { }
    public Review(User user, ServiceProvider serviceProvider, Reservation reservation, int rating) {
        this.user = user; this.serviceProvider = serviceProvider; this.reservation = reservation; this.rating = rating;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public ServiceProvider getServiceProvider() { return serviceProvider; }
    public Reservation getReservation() { return reservation; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public boolean isModerated() { return moderated; }
    public void setModerated(boolean moderated) { this.moderated = moderated; }
}
