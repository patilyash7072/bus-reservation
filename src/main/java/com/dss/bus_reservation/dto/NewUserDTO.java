package com.dss.bus_reservation.dto;

import com.dss.bus_reservation.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewUserDTO {
    private String username;
    private String password;
    private UserRole role;
}
