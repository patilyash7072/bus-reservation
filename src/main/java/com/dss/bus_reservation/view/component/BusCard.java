package com.dss.bus_reservation.view.component;

import com.dss.bus_reservation.dto.BusReservationDTO;
import com.dss.bus_reservation.enums.BusType;
import com.dss.bus_reservation.view.BookingView;
import com.dss.bus_reservation.view.NewBookingView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BusCard extends HorizontalLayout {

    private Long busId;
    private String sourceStation;
    private String destinationStation;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate dateOfJourney;
    private BusType type;
    private Integer totalDistance;

    public BusCard() {
    }

    public void showBusCard() {
        setWidthFull();
        var vertical = new VerticalLayout();
        var horizontal = new HorizontalLayout();

        vertical.setWidthFull();
        horizontal.setWidthFull();

        var source = new H3(sourceStation);
        var duration = new H3(startTime + "         ---------------------------------------------------     " + endTime);
        var destination = new H3(destinationStation);

        horizontal.add(source, duration, destination);
        horizontal.setSizeFull();
        horizontal.setJustifyContentMode(JustifyContentMode.BETWEEN);
        horizontal.setAlignItems(FlexComponent.Alignment.CENTER);

        var bookButton = new Button("Book");
        bookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        bookButton.addClickListener(e -> bookBus());

        var type = new H5(getType().name());

        var footer = new HorizontalLayout(type, bookButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        vertical.add(horizontal, footer);
        vertical.setHeight("200px");

        add(vertical);
        addClassNames(LumoUtility.Background.CONTRAST_10);
        addClassNames(LumoUtility.BorderRadius.LARGE);
        setHeight("200px");
    }


    public void bookBus() {
        var bus = BusReservationDTO.builder()
                .id(busId)
                .sourceStation(sourceStation)
                .destinationStation(destinationStation)
                .startTime(startTime)
                .endTime(endTime)
                .dateOfJourney(dateOfJourney)
                .type(type)
                .totalDistance(totalDistance)
                .build();
        VaadinSession.getCurrent().setAttribute(BusReservationDTO.class, bus);
        UI.getCurrent().navigate(BookingView.class, busId);
    }
}
