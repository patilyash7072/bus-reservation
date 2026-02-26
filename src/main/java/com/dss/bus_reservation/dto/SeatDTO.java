package com.dss.bus_reservation.dto;

import com.dss.bus_reservation.enums.BookingStatus;
import lombok.*;
import org.jspecify.annotations.NonNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO implements Comparable<SeatDTO>{
    @Override
    public int compareTo(@NonNull SeatDTO o) {
        return this.id.intValue() - o.id.intValue();
    }

    private Long id;
    private String seatNumber;
    private Long busId;
    private BookingStatus status;
}
