package com.dss.bus_reservation.dto;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class PassengerDTO implements Comparable<PassengerDTO>{
    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private Integer age;
    private String gender;

    @Override
    public int compareTo(@NotNull PassengerDTO o) {
        return this.id.intValue() - o.id.intValue();
    }
}
