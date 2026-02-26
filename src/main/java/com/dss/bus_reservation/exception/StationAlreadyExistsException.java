package com.dss.bus_reservation.exception;

public class StationAlreadyExistsException extends RuntimeException {
    public StationAlreadyExistsException(String message) {
        super(message);
    }
}
