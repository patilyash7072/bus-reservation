package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.exception.NoBusesFoundException;
import com.dss.bus_reservation.mapper.BusCardMapper;
import com.dss.bus_reservation.service.BusReservationService;
import com.dss.bus_reservation.service.StationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Route(value = "/home", layout = MainLayout.class)
@RouteAlias(value = "/search", layout = MainLayout.class)
@PageTitle("Search Buses")
@RolesAllowed("USER")
public class SearchBusView extends VerticalLayout implements BeforeEnterObserver {


    private HorizontalLayout header;
    private VerticalLayout content;

    private ComboBox<StationDTO> fromField;
    private ComboBox<StationDTO> toField;
    private DatePicker dateOfJourney;
    Set<StationDTO> stationList = new TreeSet<>();
    private Scroller scroller;

    StationDTO removedSource;
    StationDTO removedDestination;

    @Resource
    BusReservationService busService;

    @Resource
    StationService stationService;

    @PostConstruct
    public void show() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        initLayout();
        initFields();
        configureFields();
        showFields();
        showActions();
        initScroller();

        displayLayout();
    }

    public void initLayout() {
        header = new HorizontalLayout();
        var title = new H2("Book Bus Tickets");
        header.add(title);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setAlignItems(Alignment.CENTER);


        content = new VerticalLayout();
        content.setSizeFull();
        content.setJustifyContentMode(JustifyContentMode.CENTER);
        content.setAlignItems(Alignment.CENTER);
        content.setMargin(false);
    }

    public void initFields() {
        fromField = new ComboBox<>(5);
        toField = new ComboBox<>(5);
        dateOfJourney = new DatePicker();

    }

    public void configureFields() {
        fromField.setPlaceholder("From");
        toField.setPlaceholder("To");
        dateOfJourney.setPlaceholder("Date");

        stationList.addAll(stationService.findAll());
        fromField.setItems(stationList);
        toField.setItems(stationList);

        fromField.setItemLabelGenerator(StationDTO::getName);
        toField.setItemLabelGenerator(StationDTO::getName);

        dateOfJourney.setInitialPosition(LocalDate.now());
        dateOfJourney.setMin(LocalDate.now());
        dateOfJourney.setValue(LocalDate.now());
        dateOfJourney.setMax(LocalDate.now().plusMonths(3L).withDayOfMonth(31));

        fromField.setClearButtonVisible(true);
        toField.setClearButtonVisible(true);

        fromField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                stationList.remove(event.getValue());
                if (removedSource != null) {
                    stationList.add(removedSource);
                }
                removedSource = event.getValue();
                fromField.getDataProvider().refreshAll();
                toField.getDataProvider().refreshAll();
            } else {
                if (removedSource != null) {
                    stationList.add(removedSource);
                    removedSource = null;
                    fromField.getDataProvider().refreshAll();
                    toField.getDataProvider().refreshAll();
                }
            }
        });

        toField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                stationList.remove(event.getValue());
                if (removedDestination != null) {
                    stationList.add(removedDestination);
                }
                removedDestination = event.getValue();
                fromField.getDataProvider().refreshAll();
                toField.getDataProvider().refreshAll();
            } else {
                if (removedDestination != null) {
                    stationList.add(removedDestination);
                    removedDestination = null;
                    fromField.getDataProvider().refreshAll();
                    toField.getDataProvider().refreshAll();
                }
            }
        });

    }

    public void showFields() {
        header.add(fromField, toField, dateOfJourney);
    }

    public void showActions() {
        var searchButton = new Button("Search");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> {
            try {
                showList();
            } catch (NoBusesFoundException ex) {
                Notification notification = new Notification(ex.getMessage(), 3000);
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setOpened(true);
            }
        });

        header.add(searchButton);
    }


    public void initScroller() {
        scroller = new Scroller();
        scroller.setVisible(false);
        scroller.setWidthFull();
    }


    public void showList() {
        var from = fromField.getValue();
        var to = toField.getValue();
        LocalDate date = dateOfJourney.getValue();

        var busList = busService.searchBuses(from.getId(), to.getId(), date);
        if (busList.isEmpty()) {
            throw new NoBusesFoundException("No buses found for selected route. Please select another route");
        }
        var vertical = new VerticalLayout();

        for (var bus : busList) {
            var busCard = BusCardMapper.mapToBusCard(bus);
            vertical.add(busCard);
        }

        scroller.setContent(vertical);


        vertical.setSizeFull();

        scroller.setHeightFull();
        scroller.setVisible(true);

    }

    public void displayLayout() {
        add(header, scroller);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
