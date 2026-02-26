package com.dss.bus_reservation.mapper;

import com.dss.bus_reservation.dto.PassengerDTO;
import com.dss.bus_reservation.entity.Passenger;

public class PassengerMapper {
    public static PassengerDTO mapToPassengerDTO(Passenger passengerEntity) {
        return PassengerDTO.builder()
                .id(passengerEntity.getId())
                .name(passengerEntity.getName())
                .age(passengerEntity.getAge())
                .gender(passengerEntity.getGender())
                .build();
    }

    public static Passenger mapToPassenger(PassengerDTO passengerDTO) {
        return Passenger.builder()
                .id(passengerDTO.getId())
                .name(passengerDTO.getName())
                .age(passengerDTO.getAge())
                .gender(passengerDTO.getGender())
                .build();
    }
}
