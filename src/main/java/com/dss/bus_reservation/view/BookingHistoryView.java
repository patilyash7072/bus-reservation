package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.BookingHistoryResponseDTO;
import com.dss.bus_reservation.enums.PaymentStatus;
import com.dss.bus_reservation.exception.NoRefundException;
import com.dss.bus_reservation.service.BookingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashSet;
import java.util.Set;

@Route(value = "booking-history", layout = MainLayout.class)
@PageTitle("Booking History")
@RolesAllowed("USER")
public class BookingHistoryView extends VerticalLayout implements BeforeEnterObserver {
    Grid<BookingHistoryResponseDTO> grid;

    Set<BookingHistoryResponseDTO> list = new HashSet<>();

    @Resource
    BookingService bookingService;

    @PostConstruct
    public void init() {
        setSizeFull();

        showTitle();
        initGrid();
        setGridColumns();

        add(grid);
    }

    private void showTitle() {
        add(new H3("Booking History"));
    }

    private void initList() {
        list.addAll(bookingService.getBookingHistory());
    }

    private void setGridColumns() {
        grid.addColumn(BookingHistoryResponseDTO::getSourceStation)
                .setHeader("Source")
                .setKey("Source");

        grid.addColumn(BookingHistoryResponseDTO::getDestinationStation)
                .setHeader("Destination")
                .setKey("Destination");

        grid.addColumn(BookingHistoryResponseDTO::getDateOfJourney)
                .setHeader("Date of Journey")
                .setKey("Date of Journey");

        grid.addColumn(BookingHistoryResponseDTO::getPassengerName)
                .setHeader("Passenger Name")
                .setKey("Passenger Name");

        grid.addColumn(BookingHistoryResponseDTO::getPassengerAge)
                .setHeader("Age")
                .setKey("Age");

        grid.addColumn(BookingHistoryResponseDTO::getGender)
                .setHeader("Gender")
                .setKey("Gender");

        grid.addColumn(BookingHistoryResponseDTO::getSeat)
                .setHeader("Seat")
                .setKey("Seat");

        grid.addColumn(BookingHistoryResponseDTO::getBooked_at)
                .setHeader("Booked At")
                .setKey("Booked At");

        grid.addComponentColumn(bookingHistoryResponseDTO -> {
                    var button = new Button("Cancel");
                    button.addThemeVariants(ButtonVariant.LUMO_ERROR);

                    if (bookingHistoryResponseDTO.getPaymentStatus().equals(PaymentStatus.PENDING)) {
                        button.setEnabled(false);
                    }

                    if (bookingHistoryResponseDTO.getPaymentStatus().equals(PaymentStatus.REFUNDED)) {
                        button.setEnabled(false);
                    }

                    button.addClickListener(e -> {
                        try {
                            bookingService.cancelBooking(bookingHistoryResponseDTO.getBookingId());
                            var notification = new Notification("Payment Refunded. Please check payment history");
                            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            notification.setPosition(Notification.Position.MIDDLE);
                            notification.setDuration(3000);
                            notification.setOpened(true);
                            button.setEnabled(false);
                        } catch (NoRefundException ex) {
                            var notification = new Notification(ex.getMessage());
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            notification.setPosition(Notification.Position.MIDDLE);
                            notification.setDuration(3000);
                            notification.setOpened(true);
                            button.setEnabled(true);
                        }
                    });

                    return button;
                })
                .setHeader("Action")
                .setKey("Action");
    }

    private void initGrid() {
        grid = new Grid<>();
        grid.setSizeFull();
        grid.setItems(list);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        initList();
    }
}
