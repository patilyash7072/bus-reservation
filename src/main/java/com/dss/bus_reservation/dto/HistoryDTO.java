package com.dss.bus_reservation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class HistoryDTO {
    private Long bookingItemId;
    private String busName;
    private String passengerName;
    private String seat;
    private LocalDate bookingDate;
}
