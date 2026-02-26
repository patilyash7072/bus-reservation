package com.dss.bus_reservation.entity;

import com.dss.bus_reservation.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;

    private String discountMessage;

    @ManyToOne
    User user;

    @CreationTimestamp
    @LastModifiedDate
    private LocalDateTime modified_at;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
