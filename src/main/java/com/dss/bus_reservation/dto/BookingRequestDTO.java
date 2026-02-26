package com.dss.bus_reservation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class BookingRequestDTO {
    private Long busId;
    private String username;
    private Map<Long, Long> passengerIdMappedBySeatId;
}
