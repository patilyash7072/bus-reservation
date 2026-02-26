package com.dss.bus_reservation.dto;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RouteDTO implements Comparable<RouteDTO>{
    private Long id;
    private StationDTO source;
    private StationDTO destination;
    private Integer totalDistance;

    @Override
    public int compareTo(@NotNull RouteDTO o) {
        return this.id.intValue() - o.id.intValue();
    }
}
