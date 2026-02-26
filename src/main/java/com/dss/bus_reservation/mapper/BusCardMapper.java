package com.dss.bus_reservation.mapper;

import com.dss.bus_reservation.dto.BusReservationDTO;
import com.dss.bus_reservation.view.component.BusCard;

public class BusCardMapper {
    public static BusCard mapToBusCard(BusReservationDTO busReservationDTO) {
        var busCard = new BusCard();
        busCard.setBusId(busReservationDTO.getId());
        busCard.setSourceStation(busReservationDTO.getSourceStation());
        busCard.setDestinationStation(busReservationDTO.getDestinationStation());
        busCard.setStartTime(busReservationDTO.getStartTime());
        busCard.setEndTime(busReservationDTO.getEndTime());
        busCard.setDateOfJourney(busReservationDTO.getDateOfJourney());
        busCard.setType(busReservationDTO.getType());
        busCard.setTotalDistance(busReservationDTO.getTotalDistance());

        busCard.showBusCard();

        return busCard;
    }
}
