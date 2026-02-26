package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepo extends JpaRepository<User, Integer> {
    UserDetails findByUsername(String username);
}
