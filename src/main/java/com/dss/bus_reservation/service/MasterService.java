package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.AdminBusReservationDTO;
import com.dss.bus_reservation.dto.RouteDTO;
import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.enums.BusType;
import com.dss.bus_reservation.enums.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class MasterService {

    private UserService userService;
    private StationService stationService;
    private RouteService routeService;
    private BusReservationService busReservationService;
    private SeatService seatService;

    public MasterService(UserService userService, StationService stationService, RouteService routeService, BusReservationService busReservationService, SeatService seatService) {
        this.userService = userService;
        this.stationService = stationService;
        this.routeService = routeService;
        this.busReservationService = busReservationService;
        this.seatService = seatService;

        initDB();
    }

    public void initDB() {

        //USERS
        userService.create("user", "user", UserRole.USER);
        userService.create("admin", "admin", UserRole.ADMIN);

//        //STATIONS
//        stationService.add(StationDTO.builder().name("CSMT").build());
//        stationService.add(StationDTO.builder().name("KALYAN").build());
//
//        //ROUTE
//        var source = stationService.findById(1L).get();
//        var destination = stationService.findById(2L).get();
//        RouteDTO route = RouteDTO.builder().source(source).destination(destination).totalDistance(300).build();
//        var routeEntity = routeService.add(route);
//
//        //BUSES
//        busReservationService.add(AdminBusReservationDTO.builder()
//                .startTime(LocalTime.of(10, 00))
//                .endTime(LocalTime.of(17, 30))
//                .dateOfJourney(LocalDate.of(2026, 02, 24))
//                .type(BusType.SLEEPER)
//                .routeDTO(routeEntity)
//                .build());
//
//        busReservationService.add(AdminBusReservationDTO.builder()
//                .startTime(LocalTime.of(12, 00))
//                .endTime(LocalTime.of(18, 30))
//                .dateOfJourney(LocalDate.of(2026, 02, 24))
//                .type(BusType.SEMI_SLEEPER)
//                .routeDTO(routeEntity)
//                .build());
//
//        busReservationService.add(AdminBusReservationDTO.builder()
//                .startTime(LocalTime.of(13, 00))
//                .endTime(LocalTime.of(19, 30))
//                .dateOfJourney(LocalDate.of(2026, 02, 24))
//                .type(BusType.SLEEPER)
//                .routeDTO(routeEntity)
//                .build());
//
//        //SEATS
//        BusReservation bus1 = busReservationService.findById(1l).get();
//        BusReservation bus2 = busReservationService.findById(2l).get();
//        BusReservation bus3 = busReservationService.findById(3l).get();
//
//        seatService.createSeats(bus1);
//        seatService.createSeats(bus2);
//        seatService.createSeats(bus3);
    }
}
