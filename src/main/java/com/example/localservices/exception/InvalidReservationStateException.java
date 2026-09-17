package com.example.localservices.exception;

public class InvalidReservationStateException extends RuntimeException {
    public InvalidReservationStateException(String message) { super(message); }
}
