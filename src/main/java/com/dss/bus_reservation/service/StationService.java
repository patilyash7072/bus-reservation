package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.RouteDTO;
import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.entity.Route;
import com.dss.bus_reservation.entity.Station;
import com.dss.bus_reservation.exception.RouteAlreadyExistsException;
import com.dss.bus_reservation.exception.StationAlreadyExistsException;
import com.dss.bus_reservation.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public StationDTO find(StationDTO stationDTO) {
        if (stationDTO.getId() == null) {
            return null;
        }
        Optional<StationDTO> stn = findById(stationDTO.getId());
        if (stn.isPresent()) {
            return stationDTO;
        }
        return null;
    }

    public Station findByName(String name) {
        var stn =  stationRepository.findByNameIgnoreCaseAndIsDeleted(name, false);
        return stn.orElse(null);
    }

    public List<StationDTO> findAll() {
        var stationList = stationRepository.findAll();
        return stationList.stream()
                .filter(station -> !station.isDeleted())
                .map(stop -> StationDTO.builder()
                        .id(stop.getId())
                        .name(stop.getName())
                        .build())
                .toList();
    }

    public Optional<StationDTO> findById(Long id) {
        var station = stationRepository.findById(id);
        if (station.isPresent()) {
            return station.map(stn -> StationDTO.builder()
                    .id(stn.getId())
                    .name(stn.getName())
                    .build());
        }
        return Optional.empty();
    }

    public StationDTO add(StationDTO dto) {
        var stn = findByName(dto.getName());
        if (stn != null) {
            throw new StationAlreadyExistsException("Station is already created with same name. Please use other name for station.");
        }

        var station = Station.builder().name(dto.getName()).build();
        stationRepository.save(station);

        dto.setId(station.getId());
        return dto;
    }

    @Transactional
    public StationDTO update(StationDTO dto) {
        var stn = findByName(dto.getName());
        if (stn != null) {
            throw new StationAlreadyExistsException("Station is already created with same name. Please use other name for station.");
        }


        var station = stationRepository.findById(dto.getId());
        station.ifPresent(stationEntity -> stationEntity.setName(dto.getName()));
        return dto;
    }

    @Transactional
    public void delete(StationDTO dto) {
        var station = stationRepository.findById(dto.getId());
        station.ifPresent(stn -> stn.setDeleted(true));
    }

}
