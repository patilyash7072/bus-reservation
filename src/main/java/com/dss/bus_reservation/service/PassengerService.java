package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.PassengerDTO;
import com.dss.bus_reservation.entity.Passenger;
import com.dss.bus_reservation.entity.User;
import com.dss.bus_reservation.enums.Gender;
import com.dss.bus_reservation.exception.PassengerAlreadyExistsException;
import com.dss.bus_reservation.mapper.PassengerMapper;
import com.dss.bus_reservation.repository.PassengerRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PassengerService {

    @Autowired
    private PassengerRepository repository;

    @Autowired
    private UserService userService;

    private final transient AuthenticationContext authContext;

    public PassengerService(AuthenticationContext authContext) {
        this.authContext = authContext;
    }


    public PassengerDTO save(PassengerDTO passengerDTO) {
        var passenger = PassengerMapper.mapToPassenger(passengerDTO);
        var user = (User) userService.loadUserByUsername(authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get());
        Optional<Passenger> pass = repository.findByNamIgnoreCaseeAndAgeAndGenderAndUserAndIsDeleted(passengerDTO.getName(), passengerDTO.getAge(), passengerDTO.getGender(), user, false);
        if (pass.isPresent()){
            throw new PassengerAlreadyExistsException("Passenger is already present");
        }
        passenger.setUser(user);
        Passenger saved =  repository.save(passenger);
        return PassengerMapper.mapToPassengerDTO(saved);
    }

    public PassengerDTO find(PassengerDTO passengerDTO) {
        if (passengerDTO.getId() == null) {
            return null;
        }
        var passenger = repository.findById(passengerDTO.getId());
        if (passenger.isPresent() && !passenger.get().isDeleted()) {
            return passenger.map(PassengerMapper::mapToPassengerDTO).get();
        }
        return null;
    }

    public List<Passenger> findAllByPassengerId(List<Long> passengerId) {
        return repository.findAllByIdIn(passengerId);
    }

    public List<PassengerDTO> findByUsername(String username) {
        List<Passenger> passengerList = repository.findAllByUser_UsernameAndIsDeleted(username, false);
        if (!passengerList.isEmpty()) {
            return passengerList.stream().map(PassengerMapper::mapToPassengerDTO).toList();
        }
        return null;
    }

    public List<PassengerDTO> findAll() {
        var username = authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get();

        return this.findByUsername(username);
    }

    @Transactional
    public PassengerDTO update(PassengerDTO passengerDTO) {
        var user = (User) userService.loadUserByUsername(authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get());
        Optional<Passenger> pass = repository.findByNamIgnoreCaseeAndAgeAndGenderAndUserAndIsDeleted(passengerDTO.getName(), passengerDTO.getAge(), passengerDTO.getGender(), user, false);
        if (pass.isPresent()){
            throw new PassengerAlreadyExistsException("Passenger is already present");
        }

        var passenger = repository.findById(passengerDTO.getId());
        if (passenger.isPresent()) {
            Passenger passengerEntity = passenger.get();
            passengerEntity.setName(passengerDTO.getName());
            passengerEntity.setAge(passengerDTO.getAge());
            passengerEntity.setGender(passengerDTO.getGender());
            return PassengerMapper.mapToPassengerDTO(passenger.get());
        }
        return null;
    }

    @Transactional
    public void delete(PassengerDTO passengerDTO) {
        var passenger = repository.findById(passengerDTO.getId());
        passenger.ifPresent(passengerEntity -> {
            passengerEntity.setDeleted(true);
        });
    }
}

