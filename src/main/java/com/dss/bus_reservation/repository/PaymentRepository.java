package com.dss.bus_reservation.repository;

import com.dss.bus_reservation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    public boolean existsByUser_Username(String id);
}
