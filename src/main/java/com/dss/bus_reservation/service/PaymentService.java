package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.PaymentResponseDTO;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.entity.Passenger;
import com.dss.bus_reservation.entity.Payment;
import com.dss.bus_reservation.entity.User;
import com.dss.bus_reservation.enums.BusType;
import com.dss.bus_reservation.enums.PaymentStatus;
import com.dss.bus_reservation.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    public static final int FIRST_BOOKING_DISCOUNT_PERCENT = 20;
    public static final int HIGH_NO_SEAT_DISCOUNT_PERCENT = 10;
    public static final int SENIOR_CITIZEN_DISCOUNT_PERCENT = 15;

    public static final int SEMI_SLEEPER_RATE = 2;
    public static final int SLEEPER_RATE = 3;
    public static final int HIGH_NO_SEAT = 4;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    BusReservationService busReservationService;




    public Payment initiatePayment(User user, Long busId, List<Passenger> passengerList) {

        var username = user.getUsername();

        Integer amount = calculateTotalAmount(busId);

        List discountStub = calculateDiscount(amount, username, passengerList);

        List<String> discountList = (List<String>) discountStub.get(1);
        StringBuilder discount = new StringBuilder();

        discountList.forEach(discount::append);

        var payment = Payment.builder()
                .user(user)
                .amount((Integer) discountStub.get(0))
                .discountMessage(discount.toString())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        return paymentRepository.save(payment);
    }

    public PaymentResponseDTO getPaymentDetails(Long paymentId) {

        var payment = paymentRepository.findById(paymentId);
        return payment.map(value -> PaymentResponseDTO.builder()
                .paymentId(value.getId())
                .amount(value.getAmount())
                .discountMessage(Arrays.asList(value.getDiscountMessage().split("\\.")))
                .paymentStatus(value.getPaymentStatus())
                .build()).orElse(null);
    }

    private int calculateTotalAmount(Long busId) {

        boolean isSleeper = false;

        var entity = busReservationService.findById(busId);
        if (entity.isPresent()) {
            var type = entity.get().getType();
            if (type.equals(BusType.SLEEPER)) {
                isSleeper = true;
            }
        }
        int totalDistance = entity.get().getRoute().getTotalDistance();
        if (isSleeper) {
            return SLEEPER_RATE * totalDistance;
        } else {
            return SEMI_SLEEPER_RATE * totalDistance;
        }
    }

    private List calculateDiscount(Integer amount, String username, List<Passenger> passengerList) {

        List<String> discountList = new ArrayList<>();

        amount *= passengerList.size();

        boolean isFirstBooking = !paymentRepository.existsByUser_Username(username);

        if (isFirstBooking) {
            amount = amount - (amount * FIRST_BOOKING_DISCOUNT_PERCENT / 100);
            discountList.add(FIRST_BOOKING_DISCOUNT_PERCENT + "% discount on First Booking. ");
        }
        if (passengerList.size() >= HIGH_NO_SEAT) {
            amount = amount - (amount * HIGH_NO_SEAT_DISCOUNT_PERCENT / 100);
            discountList.add(HIGH_NO_SEAT_DISCOUNT_PERCENT + "% discount for booking more than " + (HIGH_NO_SEAT - 1) + " seats. ");
        }
        boolean isSenior = passengerList.stream()
                .anyMatch(passengerDTO -> passengerDTO.getAge() >= 60);

        if (isSenior) {
            var list = passengerList
                    .stream()
                    .filter(passengerDTO -> passengerDTO.getAge() >= 60)
                    .toList();
            for (var senior : list) {
                amount = amount - (amount * SENIOR_CITIZEN_DISCOUNT_PERCENT / 100);
            }
            discountList.add(SENIOR_CITIZEN_DISCOUNT_PERCENT + "% discount on senior citizen. ");
        }

        return List.of(amount, discountList);
    }

    @Transactional
    public boolean pay(Long paymentId) {
        var entity = paymentRepository.findById(paymentId);
        if (entity.isPresent()) {
            var payment = entity.get();
            payment.setPaymentStatus(PaymentStatus.PAID);
            return true;
        } else {
            return false;
        }

    }

    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

}
