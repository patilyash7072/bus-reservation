package com.dss.bus_reservation.exception;

public class PassengerAlreadyInitiatedBookingException extends RuntimeException {
    public PassengerAlreadyInitiatedBookingException(String message) {
        super(message);
    }
}
