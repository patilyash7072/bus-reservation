package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.AdminBusReservationDTO;
import com.dss.bus_reservation.dto.BusReservationDTO;
import com.dss.bus_reservation.dto.RouteDTO;
import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.entity.Route;
import com.dss.bus_reservation.exception.BusAlreadyExistsException;
import com.dss.bus_reservation.mapper.BusReservationMapper;
import com.dss.bus_reservation.repository.BusReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BusReservationService {

    private BusReservationRepository repository;

    private RouteService routeService;

    private SeatService seatService;


    public BusReservationService(BusReservationRepository repository, RouteService routeService, SeatService seatService) {
        this.repository = repository;
        this.routeService = routeService;
        this.seatService = seatService;
    }

    public AdminBusReservationDTO find(AdminBusReservationDTO adminBusReservationDTO) {
        if (adminBusReservationDTO.getId() == null) {
            return null;
        }
        Optional<BusReservation> route = findById(adminBusReservationDTO.getId());
        if (route.isPresent()) {
            return adminBusReservationDTO;
        }
        return null;
    }
    public boolean exist(AdminBusReservationDTO dto) {
        Optional<BusReservation> entity = repository.findByRoute_IdAndStartTimeAndEndTimeAndDateOfJourneyAndType(dto.getRouteDTO().getId(), dto.getStartTime(), dto.getEndTime(), dto.getDateOfJourney(), dto.getType());
        return entity.isPresent();
    }

    public List<AdminBusReservationDTO> findAll() {
        List<BusReservation> busList = repository.findAll();
        if (!busList.isEmpty()) {
            return busList.stream()
                    .filter(bus -> !bus.isDeleted())
                    .map(bus -> {
                        var sourceDTO = StationDTO.builder().id(bus.getRoute().getSourceStation().getId()).name(bus.getRoute().getSourceStation().getName()).build();
                        var destinationDTO = StationDTO.builder().id(bus.getRoute().getDestinationStation().getId()).name(bus.getRoute().getDestinationStation().getName()).build();
                        var routeDto = RouteDTO.builder().id(bus.getRoute().getId()).source(sourceDTO).destination(destinationDTO).totalDistance(bus.getRoute().getTotalDistance()).build();
                        return AdminBusReservationDTO.builder()
                                .id(bus.getId())
                                .startTime(bus.getStartTime())
                                .endTime(bus.getEndTime())
                                .type(bus.getType())
                                .dateOfJourney(bus.getDateOfJourney())
                                .routeDTO(routeDto)
                                .build();
                    }).toList();
        } else {
            return null;
        }
    }

    public List<BusReservationDTO> searchBuses(Long sourceStationId, Long destinationStationId, LocalDate dateOfJourney) {
        var busReservationList = repository.findByRoute_SourceStation_IdAndRoute_DestinationStation_IdAndDateOfJourney(sourceStationId, destinationStationId, dateOfJourney);
        return busReservationList
                .map(busReservations -> busReservations
                        .stream()
                        .filter(bus -> !bus.isDeleted())
                        .map(BusReservationMapper::mapToBusReservationDTO)
                        .toList())
                .orElse(null);
    }

    public Optional<BusReservation> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<BusReservationDTO> getBusDetails(Long id) {
        var bus = repository.findById(id);
        return bus.map(b -> BusReservationDTO.builder()
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .sourceStation(b.getRoute().getSourceStation().getName())
                .destinationStation(b.getRoute().getDestinationStation().getName())
                .totalDistance(b.getRoute().getTotalDistance())
                .dateOfJourney(b.getDateOfJourney())
                .type(b.getType())
                .build());
    }

    public AdminBusReservationDTO add(AdminBusReservationDTO dto) {
        if (exist(dto)) throw new BusAlreadyExistsException("Bus already exists for specified details. Please provide different details");
        Optional<Route> route = routeService.findById(dto.getRouteDTO().getId());
        BusReservation saved = null;
        if (route.isPresent()) {
            BusReservation bus = BusReservation.builder()
                    .route(route.get())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .dateOfJourney(dto.getDateOfJourney())
                    .type(dto.getType())
                    .build();

            saved = repository.save(bus);
            seatService.createSeats(saved);
        }
        dto.setId(saved.getId());
        return dto;
    }

    @Transactional
    public AdminBusReservationDTO update(AdminBusReservationDTO dto) {
        if (exist(dto)) throw new BusAlreadyExistsException("Bus already exists for specified details. Please provide different details");
        var busReservation = repository.findById(dto.getId());
        if (busReservation.isPresent()) {
            var route = routeService.findById(dto.getRouteDTO().getId());
            if (route.isPresent()) {
                var bus = busReservation.get();
                bus.setRoute(route.get());
                bus.setStartTime(dto.getStartTime());
                bus.setEndTime(dto.getEndTime());
                bus.setDateOfJourney(dto.getDateOfJourney());
                bus.setType(dto.getType());
            }
        }
        return dto;
    }

    @Transactional
    public void delete(AdminBusReservationDTO dto) {
        var entity = repository.findById(dto.getId());
        entity.ifPresent(busReservation -> busReservation.setDeleted(true));
    }
}
