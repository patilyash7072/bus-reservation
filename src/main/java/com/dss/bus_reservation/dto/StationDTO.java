package com.dss.bus_reservation.dto;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class StationDTO implements Comparable<StationDTO>{
    private Long id;
    private String name;

    @Override
    public int compareTo(@NotNull StationDTO o) {
        return this.id.intValue() - o.id.intValue();
    }
}
