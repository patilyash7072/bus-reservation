package com.dss.bus_reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Payment payment;

    @ManyToOne
    private BusReservation busReservation;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "booking")
    private List<BookingItem> bookingItemList;
}
