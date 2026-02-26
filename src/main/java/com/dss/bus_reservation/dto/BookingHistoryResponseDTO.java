package com.dss.bus_reservation.dto;

import com.dss.bus_reservation.enums.BookingStatus;
import com.dss.bus_reservation.enums.PaymentStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class BookingHistoryResponseDTO {
    private Long bookingId;
    private Long paymentId;
    private String sourceStation;
    private String destinationStation;
    private LocalDate dateOfJourney;
    private String passengerName;
    private Integer passengerAge;
    private String gender;
    private String seat;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime booked_at;
}
