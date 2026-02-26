package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByNameIgnoreCaseAndIsDeleted(String name, boolean deleted);
}
