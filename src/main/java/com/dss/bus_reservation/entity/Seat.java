package com.dss.bus_reservation.entity;

import com.dss.bus_reservation.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    @ManyToOne
    private BusReservation busReservation;


    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
