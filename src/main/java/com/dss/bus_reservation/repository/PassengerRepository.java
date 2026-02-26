package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Passenger;
import com.dss.bus_reservation.entity.User;
import com.dss.bus_reservation.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    public List<Passenger> findAllByIdIn(List<Long> passengerIdList);
    List<Passenger> findAllByUser_UsernameAndIsDeleted(String username, Boolean isDeleted);
    Optional<Passenger> findByNamIgnoreCaseeAndAgeAndGenderAndUserAndIsDeleted(String name, Integer age, String gender, User user, Boolean isDeleted);
}
