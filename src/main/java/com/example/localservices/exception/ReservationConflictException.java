package com.example.localservices.exception;

public class ReservationConflictException extends RuntimeException {
    public ReservationConflictException(String message) { super(message); }
}
