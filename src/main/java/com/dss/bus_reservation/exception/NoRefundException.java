package com.dss.bus_reservation.exception;

public class NoRefundException extends RuntimeException {
    public NoRefundException(String message) {
        super(message);
    }
}
