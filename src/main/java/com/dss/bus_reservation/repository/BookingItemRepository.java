package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Booking;
import com.dss.bus_reservation.entity.BookingItem;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    List<BookingItem> findByBooking(Booking booking);

    Boolean existsByBooking_BusReservationAndPassenger(BusReservation busReservation, Passenger passenger);
}
