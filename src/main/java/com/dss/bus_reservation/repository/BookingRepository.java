package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Booking;
import com.dss.bus_reservation.entity.Payment;
import com.dss.bus_reservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByPayment(Payment payment);
    List<Booking> findByUser(User user);
}
