package com.hambooking.backend.exception;

public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public static class TimeSlotNotAvailableException extends RuntimeException {

        public TimeSlotNotAvailableException(String message) {
            super(message);
        }
    }
}