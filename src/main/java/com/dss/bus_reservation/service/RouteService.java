package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.RouteDTO;
import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.entity.Route;
import com.dss.bus_reservation.exception.RouteAlreadyExistsException;
import com.dss.bus_reservation.repository.RouteRepository;
import com.dss.bus_reservation.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RouteService {


    private RouteRepository routeRepository;

    private StationRepository stationRepository;

    public Optional<Route> findById(Long id) {
        return routeRepository.findById(id);
    }

    public RouteDTO findBySourceIdAndDestinationId(Long sourceId, Long destination) {
        Optional<Route> route = routeRepository.findBySourceStation_IdAndDestinationStation_Id(sourceId, destination);
        if (route.isPresent()) {
            var source = route.get().getSourceStation();
            var sourceDto = StationDTO.builder()
                    .id(source.getId())
                    .name(source.getName()).build();
            var destinationEntity = route.get().getDestinationStation();
            var destinationDto = StationDTO.builder()
                    .id(destinationEntity.getId())
                    .name(source.getName()).build();
            return RouteDTO.builder()
                    .source(sourceDto)
                    .destination(destinationDto)
                    .totalDistance(route.get().getTotalDistance())
                    .build();
        }
        return null;
    }

    public RouteDTO find(RouteDTO routeDTO) {
        if (routeDTO.getId() == null) {
            return null;
        }
        Optional<Route> route = findById(routeDTO.getId());
        if (route.isPresent()) {
            return routeDTO;
        }
        return null;
    }


    public List<RouteDTO> findAll() {
        List<Route> routeList = routeRepository.findAll();
        if (!routeList.isEmpty()) {
            return routeList.stream()
                    .filter(route -> !route.isDeleted())
                    .map(route -> {
                        var sourceDTO = StationDTO.builder().id(route.getSourceStation().getId()).name(route.getSourceStation().getName()).build();
                        var destinationDTO = StationDTO.builder().id(route.getDestinationStation().getId()).name(route.getDestinationStation().getName()).build();
                        return RouteDTO.builder()
                                .id(route.getId())
                                .source(sourceDTO)
                                .destination(destinationDTO)
                                .totalDistance(route.getTotalDistance())
                                .build();
                    }).toList();
        } else {
            return null;
        }
    }

    public RouteService(RouteRepository routeRepository, StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;

    }

    public RouteDTO add(RouteDTO dto) {
        var source = stationRepository.findById(dto.getSource().getId());
        var destination = stationRepository.findById(dto.getDestination().getId());

        var routeDTO = findBySourceIdAndDestinationId(dto.getSource().getId(), dto.getDestination().getId());
        if (routeDTO != null) {
            throw new RouteAlreadyExistsException("Route is already created with specified stations. Please use other stations.");
        }

       Route entity = null;
        if (source.isPresent() && destination.isPresent()) {
            var route = Route.builder()
                    .sourceStation(source.get())
                    .destinationStation(destination.get())
                    .totalDistance(dto.getTotalDistance())
                    .build();
            entity = routeRepository.save(route);
        }
        dto.setId(entity.getId());
        return dto;
    }

    @Transactional
    public RouteDTO update(RouteDTO dto) {


        var routeDTO = findBySourceIdAndDestinationId(dto.getSource().getId(), dto.getDestination().getId());
        if (routeDTO != null && Objects.equals(dto.getTotalDistance(), routeDTO.getTotalDistance())) {
            throw new RouteAlreadyExistsException("Route is already created with specified stations. Please use other stations.");
        }

        var entity = routeRepository.findById(dto.getId());
        entity.ifPresent(route -> {
            var source = stationRepository.findById(dto.getSource().getId());
            var destination = stationRepository.findById(dto.getDestination().getId());
            if (source.isPresent() && destination.isPresent()) {
                route.setSourceStation(source.get());
                route.setDestinationStation(destination.get());
                route.setTotalDistance(dto.getTotalDistance());
            }
        });
        return dto;
    }


    @Transactional
    public void delete(RouteDTO routeDTO) {
        Optional<Route> route = routeRepository.findById(routeDTO.getId());
        route.ifPresent(value -> value.setDeleted(true));
    }
}
