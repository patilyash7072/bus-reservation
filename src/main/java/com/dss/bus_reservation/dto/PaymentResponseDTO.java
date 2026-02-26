package com.dss.bus_reservation.dto;

import com.dss.bus_reservation.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaymentResponseDTO {
    private Long paymentId;
    private Integer amount;
    private List<String> discountMessage;
    private PaymentStatus paymentStatus;
}
