package com.dss.bus_reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {
    private Long from;
    private Long to;
    private LocalDate dateOfJourney;
}
