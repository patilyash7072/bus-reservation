package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.entity.Route;
import com.dss.bus_reservation.enums.BusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BusReservationRepository extends JpaRepository<BusReservation, Long> {

    public Optional<List<BusReservation>> findByRoute_SourceStation_IdAndRoute_DestinationStation_IdAndDateOfJourney(Long source, Long destination, LocalDate dateOfJourney);

    Optional<BusReservation> findByRoute_IdAndStartTimeAndEndTimeAndDateOfJourneyAndType(Long id, LocalTime start, LocalTime end, LocalDate dateOfJourney, BusType type);
}
