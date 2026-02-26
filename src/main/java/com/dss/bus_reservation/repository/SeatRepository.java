package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Seat;
import com.dss.bus_reservation.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    public Optional<List<Seat>> findByBusReservation_IdAndBookingStatus(Long id, BookingStatus bookingStatus);
    public Seat findBySeatNumber(String seatNumber);
}
