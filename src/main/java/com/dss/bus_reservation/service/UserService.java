package com.dss.bus_reservation.service;


import com.dss.bus_reservation.enums.UserRole;
import com.dss.bus_reservation.exception.UserAlreadyRegisteredException;
import com.dss.bus_reservation.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.dss.bus_reservation.entity.User;

@Service
public class UserService implements UserDetailsService {

    private UserRepo userRepo;


    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username);
    }

    public void create(String username, String password, UserRole role) {

        var userEntity = userRepo.findByUsername(username.toLowerCase());

        if (userEntity != null) {
            throw new UserAlreadyRegisteredException("User with username: " + username + " already registered. Please use another username");
        }

        User user = User.builder()
                .username(username.toLowerCase())
                .password(new BCryptPasswordEncoder().encode(password))
                .role(role)
                .build();

        userRepo.save(user);
    }
}
