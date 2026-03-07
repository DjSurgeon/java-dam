package com.hambooking.backend.exception;

public class TimeSlotNotAvailableException extends RuntimeException {

    public TimeSlotNotAvailableException(String message) {
        super(message);
    }
}