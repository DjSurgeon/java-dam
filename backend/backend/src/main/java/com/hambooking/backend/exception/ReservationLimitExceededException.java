package com.hambooking.backend.exception;

public class ReservationLimitExceededException extends RuntimeException {

    public ReservationLimitExceededException(String message) {
        super(message);
    }
}