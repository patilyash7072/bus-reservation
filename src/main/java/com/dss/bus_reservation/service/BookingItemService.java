package com.dss.bus_reservation.service;

import com.dss.bus_reservation.entity.*;
import com.dss.bus_reservation.repository.BookingItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingItemService {

    @Autowired
    private BookingItemRepository bookingItemRepository;

    public BookingItem save(Booking booking, Passenger passsenger, Seat seat) {
        var bookingItem = BookingItem.builder()
                .booking(booking)
                .passenger(passsenger)
                .seat(seat)
                .build();
        return bookingItemRepository.save(bookingItem);
    }

    public boolean checkPassengerAlreadyInitiatedBooking(BusReservation busReservation, Passenger passenger){
        return bookingItemRepository.existsByBooking_BusReservationAndPassenger(busReservation, passenger);
    }

    public List<BookingItem> findByBooking(Booking booking) {
        return bookingItemRepository.findByBooking(booking);
    }
}
