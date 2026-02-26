package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findBySourceStation_IdAndDestinationStation_Id(Long sourceId, Long destinationId);
}
