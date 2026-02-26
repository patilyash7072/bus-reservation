package com.dss.bus_reservation.dto;

import lombok.*;
import org.jspecify.annotations.NonNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NewPassengerSeatDTO implements Comparable<NewPassengerSeatDTO>{
    @Override
    public int compareTo(@NonNull NewPassengerSeatDTO o) {
        return this.passengerDTO.getId().intValue() - o.passengerDTO.getId().intValue();
    }

    @EqualsAndHashCode.Include
    private PassengerDTO passengerDTO;
    private SeatDTO seatDTO;
}
