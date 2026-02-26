package com.dss.bus_reservation.view;


import com.dss.bus_reservation.dto.BusReservationDTO;
import com.dss.bus_reservation.dto.PassengerSeatDTO;
import com.dss.bus_reservation.dto.PaymentResponseDTO;
import com.dss.bus_reservation.exception.SeatAlreadyBookedException;
import com.dss.bus_reservation.service.BookingService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;

import java.util.Comparator;
import java.util.List;

@Route("/payment")
@PageTitle("Checkout")
@RolesAllowed("USER")
public class CheckoutView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    H5 busDetailsTitle = new H5();
    H5 busDetailsExtra = new H5();

    Button payButton;

    Grid<PassengerSeatDTO> grid;

    @Resource
    private BookingService bookingService;


    private PaymentResponseDTO paymentDetails;


    Long bookingId;

    public void init() {

        paymentDetails = bookingService.getPaymentDetails(bookingId);
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        getBusDetails();
        initPaymentButton();
        showTitle();
        showPassengerDetails();
        showDiscounts();

    }

    public void showTitle() {
        var title = new H3("Checkout");
        title.setWidth("150px");

        var vertical = new VerticalLayout(busDetailsTitle, busDetailsExtra);

        var amount = new H3("Total Amount : " + paymentDetails.getAmount());

        amount.setWidth("300px");

        var header = new HorizontalLayout(title, vertical, amount, payButton);

        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        add(header);
    }

    public void getBusDetails() {

        BusReservationDTO bus = VaadinSession.getCurrent().getAttribute(BusReservationDTO.class);

        String busString = bus.getSourceStation() + "-----" + bus.getDestinationStation();
        busDetailsTitle.setText(busString);

        String busDetailsExtraString = bus.getStartTime() + "-----" + bus.getEndTime() +
                ", " + bus.getDateOfJourney() + ", " + bus.getType();

        busDetailsExtra.setText(busDetailsExtraString);

    }

    public void showPassengerDetails() {
        grid = new Grid<>();
        grid.setWidthFull();

        grid.addColumn(PassengerSeatDTO::getName)
                .setHeader("Name")
                .setSortable(true)
                .setComparator(Comparator.comparing(PassengerSeatDTO::getName, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Name");

        grid.addColumn(PassengerSeatDTO::getAge)
                .setHeader("Age")
                .setSortable(true)
                .setKey("Age");

        grid.addColumn(PassengerSeatDTO::getGender)
                .setHeader("Gender")
                .setSortable(true)
                .setComparator(Comparator.comparing(PassengerSeatDTO::getGender, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Gender");

        grid.addColumn(PassengerSeatDTO::getSeat)
                .setHeader("Seat")
                .setSortable(true)
                .setComparator(Comparator.comparing(PassengerSeatDTO::getSeat, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Seat");


        List<PassengerSeatDTO> passengerSeatDTOList = (List<PassengerSeatDTO>) VaadinSession.getCurrent().getAttribute("passList");
        grid.setItems(passengerSeatDTOList);


        add(grid);


    }


    public void showDiscounts() {
        if (!paymentDetails.getDiscountMessage().isEmpty()) {
            paymentDetails.getDiscountMessage().forEach(discount -> {
                var discountMessage = new H3(discount);
                add(discountMessage);
            });
        }
    }

    public void initPaymentButton() {
        payButton = new Button("Pay");
        payButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        payButton.addClickListener(e -> {
            try {
                boolean isSuccess = bookingService.pay(paymentDetails.getPaymentId());
                if (isSuccess) {
                    Notification notification = new Notification("Payment Successful", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.setOpened(true);
                    UI.getCurrent().navigate(BookingHistoryView.class);
                } else {
                    Notification notification = new Notification("Payment Failed", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setOpened(true);
                    UI.getCurrent().navigate(SearchBusView.class);
                }
            } catch (SeatAlreadyBookedException ex) {
                Notification notification = new Notification(ex.getMessage(), 3000);
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setOpened(true);
            }

        });
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        init();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long id) {
        bookingId = id;
    }


}
