package com.dss.bus_reservation.dto;

import com.dss.bus_reservation.enums.BusType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusReservationDTO {
    private Long id;
    private String sourceStation;
    private String destinationStation;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate dateOfJourney;
    private BusType type;
    private Integer totalDistance;
}
