package com.dss.bus_reservation.exception;

public class RouteAlreadyExistsException extends RuntimeException {
    public RouteAlreadyExistsException(String message) {
        super(message);
    }
}
