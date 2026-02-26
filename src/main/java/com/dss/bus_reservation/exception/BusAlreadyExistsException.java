package com.dss.bus_reservation.exception;

public class BusAlreadyExistsException extends RuntimeException {
    public BusAlreadyExistsException(String message) {
        super(message);
    }
}
