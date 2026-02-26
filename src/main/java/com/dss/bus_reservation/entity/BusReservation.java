package com.dss.bus_reservation.entity;

import com.dss.bus_reservation.enums.BusType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Route route;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate dateOfJourney;

    @Enumerated(EnumType.STRING)
    private BusType type;

    private boolean isDeleted = false;

}
