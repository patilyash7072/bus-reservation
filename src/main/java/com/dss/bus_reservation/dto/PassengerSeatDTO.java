package com.dss.bus_reservation.dto;

import lombok.*;

import java.sql.Array;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"id","seatId","seat"})
public class PassengerSeatDTO {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private Long seatId;
    private String seat;
}
