package com.dss.bus_reservation.exception;

public class NoBusesFoundException extends RuntimeException {
    public NoBusesFoundException(String message) {
        super(message);
    }
}
