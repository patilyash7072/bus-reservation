package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.BusReservationDTO;
import com.dss.bus_reservation.dto.SeatDTO;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.entity.Seat;
import com.dss.bus_reservation.enums.BookingStatus;
import com.dss.bus_reservation.enums.BusType;
import com.dss.bus_reservation.exception.SeatAlreadyBookedException;
import com.dss.bus_reservation.mapper.SeatMapper;
import com.dss.bus_reservation.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    private SeatRepository seatRepository;


    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }


    public void createSeats(BusReservation busReservation) {
        List<SeatDTO> seatDTOList = new ArrayList<>();

        if (busReservation.getType().equals(BusType.SLEEPER)) {
            for (int i = 1; i <= 50; i++) {
                var seatNumber = "S" + i;
                seatDTOList.add(SeatDTO.builder().seatNumber(seatNumber).status(BookingStatus.AVAILABLE).build());
            }
        } else {
            for (int i = 1; i <= 25; i++) {
                var seatNumber = "A" + i;
                seatDTOList.add(SeatDTO.builder().seatNumber(seatNumber).status(BookingStatus.AVAILABLE).build());
            }
            for (int i = 26; i <= 50; i++) {
                var seatNumber = "S" + i;
                seatDTOList.add(SeatDTO.builder().seatNumber(seatNumber).status(BookingStatus.AVAILABLE).build());
            }
        }

        List<Seat> seatList = seatDTOList.stream()
                .map(seat -> SeatMapper.maptoSeat(seat, busReservation))
                .toList();
        seatRepository.saveAll(seatList);
    }

    public List<SeatDTO> getAvailableSeats(Long busReservationId) {
        var availableSeats = seatRepository.findByBusReservation_IdAndBookingStatus(busReservationId, BookingStatus.AVAILABLE);
        return availableSeats.map(seats -> seats
                .stream()
                .map(SeatMapper::maptoSeatDTO)
                .toList()).orElse(null);
    }

    public Seat findSeatById(Long seatId) {
        return seatRepository.findById(seatId).get();
    }


    @Transactional
    public void setStatusBooked(Long id) {
        var seatEntity = seatRepository.findById(id);
        seatEntity.ifPresent(seat -> {
            if (seat.getBookingStatus().equals(BookingStatus.BOOKED)) {
                throw new SeatAlreadyBookedException("Seat is already booked by other passenger.");
            }
            seat.setBookingStatus(BookingStatus.BOOKED);
        });
    }

    @Transactional
    public void setStatusAvailable(Long id) {
        var seat = seatRepository.findById(id);
        seat.ifPresent(value -> value.setBookingStatus(BookingStatus.AVAILABLE));
    }
}
