package com.dss.bus_reservation.dto;

import com.dss.bus_reservation.enums.BusType;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AdminBusReservationDTO implements Comparable<AdminBusReservationDTO> {
    private Long id;
    private RouteDTO routeDTO;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate dateOfJourney;
    private BusType type;

    @Override
    public int compareTo(@NotNull AdminBusReservationDTO o) {
        return this.id.intValue() - o.id.intValue();
    }
}
