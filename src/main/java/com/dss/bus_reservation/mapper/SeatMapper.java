package com.dss.bus_reservation.mapper;

import com.dss.bus_reservation.dto.SeatDTO;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.entity.Seat;
import com.dss.bus_reservation.enums.BookingStatus;

public class SeatMapper {
    public static SeatDTO maptoSeatDTO(Seat seat) {
        return SeatDTO.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .busId(seat.getBusReservation().getId())
                .status(seat.getBookingStatus())
                .build();
    }

    public static Seat maptoSeat(SeatDTO seatDTO, BusReservation busReservation) {
        return Seat.builder()
                .seatNumber(seatDTO.getSeatNumber())
                .bookingStatus(BookingStatus.AVAILABLE)
                .busReservation(busReservation)
                .bookingStatus(seatDTO.getStatus())
                .build();
    }
}
