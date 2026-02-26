package com.dss.bus_reservation.service;

import com.dss.bus_reservation.dto.BookingHistoryResponseDTO;
import com.dss.bus_reservation.dto.BookingRequestDTO;
import com.dss.bus_reservation.dto.BookingResponseDTO;
import com.dss.bus_reservation.dto.PaymentResponseDTO;
import com.dss.bus_reservation.entity.*;
import com.dss.bus_reservation.enums.BookingStatus;
import com.dss.bus_reservation.enums.PaymentStatus;
import com.dss.bus_reservation.exception.NoRefundException;
import com.dss.bus_reservation.exception.PassengerAlreadyInitiatedBookingException;
import com.dss.bus_reservation.repository.BookingRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    UserService userService;

    @Autowired
    BusReservationService busReservationService;

    @Autowired
    PassengerService passengerService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingItemService bookingItemService;

    @Autowired
    SeatService seatService;

    private final transient AuthenticationContext authContext;

    public BookingService(AuthenticationContext authContext) {
        this.authContext = authContext;
    }


    public List<BookingHistoryResponseDTO> getBookingHistory() {
        var user = (User) userService.loadUserByUsername(authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get());
        List<Booking> bookingList = bookingRepository.findByUser(user);
        List<BookingHistoryResponseDTO> bookingResponseList = new ArrayList<>();

        for(var booking : bookingList) {
            List<BookingItem> bookingItemList = bookingItemService.findByBooking(booking);
            bookingItemList.forEach(bookingItem -> {
                var bookingResponse = BookingHistoryResponseDTO.builder()
                        .bookingId(booking.getId())
                        .sourceStation(booking.getBusReservation().getRoute().getSourceStation().getName())
                        .destinationStation(booking.getBusReservation().getRoute().getDestinationStation().getName())
                        .dateOfJourney(booking.getBusReservation().getDateOfJourney())
                        .passengerName(bookingItem.getPassenger().getName())
                        .passengerAge(bookingItem.getPassenger().getAge())
                        .gender(bookingItem.getPassenger().getGender())
                        .seat(bookingItem.getSeat().getSeatNumber())
                        .booked_at(booking.getPayment().getModified_at())
                        .paymentStatus(booking.getPayment().getPaymentStatus())
                        .paymentId(booking.getPayment().getId())
                        .build();

                bookingResponseList.add(bookingResponse);
            });
        }

        return bookingResponseList;
    }

    public BookingResponseDTO bookSeat(BookingRequestDTO bookingDTO) {
        var user = userService.loadUserByUsername(bookingDTO.getUsername());

        var passengerList = passengerService.findAllByPassengerId(bookingDTO.getPassengerIdMappedBySeatId().keySet().stream().toList());

        var payment = paymentService.initiatePayment((User) user, bookingDTO.getBusId(), passengerList);

        BusReservation busReservation = busReservationService.findById(bookingDTO.getBusId()).get();

        Map<Long, Long> passengerIdMappedBySeatId = bookingDTO.getPassengerIdMappedBySeatId();


        Booking booking = Booking.builder()
                .user((User) user)
                .busReservation(busReservation)
                .payment(payment)
                .build();


        var bookingEntity = bookingRepository.save(booking);

        var bookingItemList = passengerList.stream().map(passenger -> {
            var exists = bookingItemService.checkPassengerAlreadyInitiatedBooking(busReservation, passenger);
            if (exists) {
                throw new PassengerAlreadyInitiatedBookingException("Passenger already initiated booking or already booked for same bus. Please book for another passenger.");
            }
            var seatId = passengerIdMappedBySeatId.get(passenger.getId());
            var seat = seatService.findSeatById(seatId);
            return bookingItemService.save(booking, passenger, seat);
        }).toList();


        bookingEntity.setBookingItemList(bookingItemList);

        return BookingResponseDTO.builder().bookingId(bookingEntity.getId()).build();
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        var booking = bookingRepository.findById(bookingId);
        booking.ifPresent(e ->  {
            var bookingTime = booking.get().getPayment().getModified_at();
            if (bookingTime.isBefore(LocalDateTime.now().minusSeconds(30L))) {
                throw new NoRefundException("No refund is generated as more than 30 seconds is passed");
            }
            booking.get().getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
            var itemList = bookingItemService.findByBooking(booking.get());
            itemList.forEach(item -> item.getSeat().setBookingStatus(BookingStatus.AVAILABLE));
        });
    }

    @Transactional
    public PaymentResponseDTO getPaymentDetails(Long bookingId) {
        var booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            var paymentId = booking.get().getPayment().getId();
            var payment = paymentService.getPaymentDetails(paymentId);
            return payment;
        }
        return null;
    }

    @Transactional
    public boolean pay(Long paymentId) {
        Payment payment = paymentService.findById(paymentId).get();
        Booking booking = bookingRepository.findByPayment(payment);
        var bookingItem = bookingItemService.findByBooking(booking);
        bookingItem.forEach(e -> seatService.setStatusBooked(e.getSeat().getId()));
        return paymentService.pay(paymentId);
    }
}
