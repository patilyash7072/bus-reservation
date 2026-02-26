package com.dss.bus_reservation.mapper;

import com.dss.bus_reservation.dto.NewPassengerSeatDTO;
import com.dss.bus_reservation.dto.PassengerSeatDTO;

public class NewPassSeatMapper {
    public static PassengerSeatDTO toOld(NewPassengerSeatDTO dto){
        return PassengerSeatDTO.builder()
                .name(dto.getPassengerDTO().getName())
                .age(dto.getPassengerDTO().getAge())
                .gender(dto.getPassengerDTO().getGender())
                .seat(dto.getSeatDTO().getSeatNumber())
                .seatId(dto.getSeatDTO().getId())
                .build();
    }
}
