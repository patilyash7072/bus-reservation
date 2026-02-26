package com.dss.bus_reservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Station sourceStation;

    @ManyToOne
    private Station destinationStation;


    private Integer totalDistance;


    private boolean isDeleted;

}
