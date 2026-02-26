package com.dss.bus_reservation.mapper;

import com.dss.bus_reservation.dto.BusReservationDTO;
import com.dss.bus_reservation.entity.BusReservation;

public class BusReservationMapper {
    public static BusReservationDTO mapToBusReservationDTO(BusReservation busReservationEntity) {
        var busReservationDTO = BusReservationDTO.builder()
                .id(busReservationEntity.getId())
                .sourceStation(busReservationEntity.getRoute().getSourceStation().getName())
                .destinationStation(busReservationEntity.getRoute().getDestinationStation().getName())
                .startTime(busReservationEntity.getStartTime())
                .endTime(busReservationEntity.getEndTime())
                .dateOfJourney(busReservationEntity.getDateOfJourney())
                .type(busReservationEntity.getType())
                .totalDistance(busReservationEntity.getRoute().getTotalDistance())
                .build();
        return busReservationDTO;
    }
}
