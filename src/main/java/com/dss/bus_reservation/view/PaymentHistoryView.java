package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.BookingHistoryResponseDTO;
import com.dss.bus_reservation.enums.PaymentStatus;
import com.dss.bus_reservation.service.BookingService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
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

@Route(value = "payment-history", layout = MainLayout.class)
@PageTitle("Payment History")
@RolesAllowed("USER")
public class PaymentHistoryView extends VerticalLayout implements BeforeEnterObserver {
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
        initList();

        add(grid);
    }

    private void showTitle() {
        add(new H3("Payment History"));
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

                    var button = new Button("Pay");
                    button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    button.addClickListener(e -> {
                        UI.getCurrent().navigate(CheckoutView.class, bookingHistoryResponseDTO.getPaymentId());
                    });

                    if (bookingHistoryResponseDTO.getPaymentStatus().equals(PaymentStatus.PAID)) {
                        button.setText("PAID");
                        button.setEnabled(false);
                    } else if (bookingHistoryResponseDTO.getPaymentStatus().equals(PaymentStatus.REFUNDED)) {
                        button.setText("REFUNDED");
                        button.setEnabled(false);
                    }
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
        grid.getDataProvider().refreshAll();
    }
}
