package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.*;
import com.dss.bus_reservation.entity.BusReservation;
import com.dss.bus_reservation.enums.Gender;
import com.dss.bus_reservation.exception.PassengerAlreadyExistsException;
import com.dss.bus_reservation.exception.PassengerAlreadyInitiatedBookingException;
import com.dss.bus_reservation.service.BookingService;
import com.dss.bus_reservation.service.BusReservationService;
import com.dss.bus_reservation.service.PassengerService;
import com.dss.bus_reservation.service.SeatService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Route(value = "book", layout = MainLayout.class)
@PageTitle("Book Bus Tickets")
@RolesAllowed("USER")
public class BookingView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    H5 busDetailsTitle;
    H5 busDetailsExtra;

    Dialog dialog;
    Grid<PassengerDTO> passGrid;
    Grid<SeatDTO> seatGrid;
    Grid<PassengerSeatDTO> grid;

    FormLayout form;
    Binder<PassengerDTO> passengerDTOBinder;

    TextField nameField;
    NumberField ageField;
    ComboBox<String> genderField;


    Long busResId;

    Button addPassengerSeatButton;
    Button deletePassengerSeatButton;

    Button bookButton;

    List<PassengerSeatDTO> passengerList = new ArrayList<>();
    Set<PassengerDTO> userPassengerList = new TreeSet<>();
    List<SeatDTO> seatList = new ArrayList<>();

    Map<Long, Long> passengerIdMappedBySeatId = new HashMap<>();

    private final transient AuthenticationContext authContext;

    private BusReservation bus;

    @Resource
    PassengerService passengerService;

    @Resource
    BusReservationService busReservationService;


    @Resource
    BookingService bookingService;

    @Resource
    SeatService seatService;

    public BookingView(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

    @PostConstruct
    public void init() {

        setSizeFull();
        displayTitle();
        initFormFields();
        configureBinder();
        displayPassengerAndSeatGrid();
        displayToolBar();
        initList();
        displayGrid();
    }

    public void displayTitle() {
        var title = new H3("Book Seats");
        title.setWidth("150px");


        busDetailsTitle = new H5();
        busDetailsExtra = new H5();


        var vertical = new VerticalLayout(busDetailsTitle, busDetailsExtra);

        var header = new HorizontalLayout(title, vertical);

        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        add(header);
    }

    public void getBusDetails() {
        Optional<BusReservationDTO> bus = busReservationService.getBusDetails(busResId);

        if (bus.isPresent()) {
            String busString = bus.get().getSourceStation() + "-----" + bus.get().getDestinationStation();
            busDetailsTitle.setText(busString);

            String busDetailsExtraString = bus.get().getStartTime() + "-----" + bus.get().getEndTime() +
                    ", " + bus.get().getDateOfJourney() + ", " + bus.get().getType();

            busDetailsExtra.setText(busDetailsExtraString);

        }
    }

    public void displayToolBar() {

        addPassengerSeatButton = new Button("Save Mapped Seat");
        deletePassengerSeatButton = new Button("Delete Mapped Seat");


        addPassengerSeatButton.setEnabled(false);
        addPassengerSeatButton.setDisableOnClick(true);

        deletePassengerSeatButton.setEnabled(false);
        deletePassengerSeatButton.setDisableOnClick(true);

        bookButton = new Button("Book");
        bookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        deletePassengerSeatButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        bookButton.setEnabled(false);


        bookButton.addClickListener(e -> {

            var username = authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get();
            var bookingDTO = BookingRequestDTO.builder()
                    .busId(busResId)
                    .username(username)
                    .passengerIdMappedBySeatId(passengerIdMappedBySeatId)
                    .build();
            try {
                BookingResponseDTO bookingResponse = bookingService.bookSeat(bookingDTO);

                VaadinSession.getCurrent().setAttribute("passList", passengerList);

                UI.getCurrent().navigate(CheckoutView.class, bookingResponse.getBookingId());
            } catch (PassengerAlreadyInitiatedBookingException ex) {
                Notification notification = new Notification(ex.getMessage(), 3000);
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setOpened(true);
            }

        });

        addPassengerSeatButton.addClickListener(this::addPassenger);
        deletePassengerSeatButton.addClickListener(this::confirmDelete);

        var horizontal = new HorizontalLayout(addPassengerSeatButton, deletePassengerSeatButton, bookButton);

        horizontal.setWidthFull();
        horizontal.setJustifyContentMode(JustifyContentMode.CENTER);

        add(horizontal);
    }

    public void initFormFields() {
        nameField = new TextField("Name");
        ageField = new NumberField("Age");
        genderField = new ComboBox<>("Gender");
        genderField.setItems(Arrays.stream(Gender.values()).map(Enum::toString).toList());


        nameField.setAutocomplete(Autocomplete.OFF);
        ageField.setAutocomplete(Autocomplete.OFF);


        nameField.setPlaceholder("Enter name");
        ageField.setPlaceholder("Enter age");
        genderField.setPlaceholder("Select gender");

    }

    public void configureBinder() {
        passengerDTOBinder = new Binder<>();

        passengerDTOBinder.forField(nameField)
                .asRequired("Name cannot be empty")
                .withValidator(stn -> stn.matches("^[A-Za-z]+$"), "Name should contain only letters")
                .withValidator(e -> e.length() < 100, "Total characters should be less than 100")
                .bind(PassengerDTO::getName, PassengerDTO::setName);

        passengerDTOBinder.forField(ageField)
                .asRequired("Age cannot be empty")
                .withValidator(e -> e > 0, "Age cannot be zero or negative")
                .withValidator(e -> e < 250, "Age cannot be greater than 250")
                .withConverter(Double::intValue, integer -> {
                    if (integer != null) {
                        return integer.doubleValue();
                    }
                    return Double.parseDouble("18");
                })
                .bind(PassengerDTO::getAge, PassengerDTO::setAge);

        passengerDTOBinder.forField(genderField)
                .asRequired("Gender cannot be empty")
                .bind(PassengerDTO::getGender, PassengerDTO::setGender);


    }

    void openDialog() {
        var header = "Add New Passenger";
        var dialogTitle = new H3(header);

        form = new FormLayout();

        form.setAutoResponsive(true);
        form.setExpandFields(true);
        form.setColumnWidth("10em");

        var firstRow = form.addFormRow();
        firstRow.add(nameField, 2);

        var secondRow = form.addFormRow();
        secondRow.add(ageField, 1);
        secondRow.add(genderField, 1);

        passengerDTOBinder.readBean(new PassengerDTO());


        var saveButton = new Button("Save", e -> {
            if (passengerDTOBinder.validate().isOk()) {
                var passengerBean = new PassengerDTO();
                try {
                    passengerDTOBinder.writeBean(passengerBean);
                    var saved = passengerService.save(passengerBean);
                    userPassengerList.add(saved);
                    passGrid.getDataProvider().refreshAll();
                    passGrid.deselectAll();
                    dialog.setOpened(false);

                } catch (PassengerAlreadyExistsException ex) {
                    Notification notification = new Notification(ex.getMessage(), 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                    notification.setOpened(true);
                } catch (ValidationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var cancelButton = new Button("Cancel", e -> dialog.setOpened(false));
        var buttonDiv = new HorizontalLayout(cancelButton, saveButton);
        buttonDiv.setSizeFull();
        buttonDiv.setJustifyContentMode(JustifyContentMode.CENTER);

        dialog = new Dialog(new VerticalLayout(dialogTitle, form, buttonDiv));
        dialog.setOpened(true);
        dialog.setMinWidth("350px");
        dialog.setMaxWidth("50%");

        add(dialog);
    }

    public void initList() {

        var username = authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get();
        var passengers = passengerService.findByUsername(username);
        if (passengers != null) {
            userPassengerList.addAll(passengers);
        }

        seatList.addAll(seatService.getAvailableSeats(busResId));

        passGrid.setItems(userPassengerList);
        seatGrid.setItems(seatList);
    }

    public void displayPassengerAndSeatGrid() {
        passGrid = new Grid<>();
        seatGrid = new Grid<>();

        passGrid.setHeightFull();
        seatGrid.setHeightFull();

        passGrid.setWidth("50%");
        seatGrid.setWidth("50%");

        passGrid.addColumn(PassengerDTO::getName)
                .setHeader("Name");

        seatGrid.addColumn(SeatDTO::getSeatNumber)
                .setHeader("Seat");


        passGrid.addSelectionListener(e -> {
            toggleAddPassengerSeatButton();
        });

        seatGrid.addSelectionListener(e -> {
            toggleAddPassengerSeatButton();
        });

        var addPassengerButton = new Button("Add Passenger");

        addPassengerButton.addClickListener(e -> openDialog());

        var horizontal = new HorizontalLayout(passGrid, seatGrid);
        horizontal.setSizeFull();

        add(addPassengerButton, horizontal);


    }

    private void toggleAddPassengerSeatButton() {
        if (passGrid.getSelectionModel().getFirstSelectedItem().isPresent() && seatGrid.getSelectionModel().getFirstSelectedItem().isPresent()) {
            addPassengerSeatButton.setEnabled(true);
        } else {
            addPassengerSeatButton.setEnabled(false);
        }
    }

    public void displayGrid() {
        grid = new Grid<>();
        grid.setSizeFull();

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

        grid.setItems(passengerList);

        grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent()) {
                deletePassengerSeatButton.setEnabled(true);
            } else {
                deletePassengerSeatButton.setEnabled(false);
            }
        });


        add(grid);

    }


    public void addPassenger(ClickEvent<Button> buttonClickEvent) {
        var passenger = passGrid.getSelectionModel().getFirstSelectedItem().get();
        var seatDTO = seatGrid.getSelectionModel().getFirstSelectedItem().get();

        passengerIdMappedBySeatId.put(passenger.getId(), seatDTO.getId());
        var listed = PassengerSeatDTO.builder()
                .id(passenger.getId())
                .seatId(seatDTO.getId())
                .seat(seatDTO.getSeatNumber())
                .name(passenger.getName())
                .gender(passenger.getGender())
                .age(passenger.getAge())
                .build();

        userPassengerList.remove(passenger);
        seatList.remove(seatDTO);
        passengerList.add(listed);

        passGrid.getDataProvider().refreshAll();
        seatGrid.getDataProvider().refreshAll();
        grid.getDataProvider().refreshAll();

        passGrid.deselectAll();
        seatGrid.deselectAll();
        grid.deselectAll();

        bookButton.setEnabled(!passengerList.isEmpty());
    }


    void confirmDelete(ClickEvent<Button> event) {
        if (grid.getSelectedItems().stream().findFirst().isPresent()) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Delete User");
            confirmDialog.setText("Are you sure to delete this user?");
            var deleteButton = new Button("Delete", (e) -> {
                var selected = grid.getSelectionModel().getFirstSelectedItem();
                selected.ifPresent(passenger -> {
                    passengerIdMappedBySeatId.remove(passenger.getId());
                    passengerList.remove(passenger);

                    PassengerDTO passengerDTO = PassengerDTO.builder()
                            .id(passenger.getId())
                            .name(passenger.getName())
                            .age(passenger.getAge())
                            .gender(passenger.getGender())
                            .build();

                    SeatDTO seatDTO = SeatDTO.builder()
                            .id(passenger.getSeatId())
                            .seatNumber(passenger.getSeat())
                            .build();

                    userPassengerList.add(passengerDTO);
                    seatList.add(seatDTO);

                    grid.deselectAll();
                    passGrid.deselectAll();
                    seatGrid.deselectAll();

                    grid.getDataProvider().refreshAll();
                    passGrid.getDataProvider().refreshAll();
                    seatGrid.getDataProvider().refreshAll();

                    deletePassengerSeatButton.setEnabled(false);

                    bookButton.setEnabled(!passengerList.isEmpty());
                });
            });


            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            deleteButton.setClassName(LumoUtility.Background.ERROR);
            confirmDialog.setConfirmButton(deleteButton);
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(e -> deletePassengerSeatButton.setEnabled(true));
            confirmDialog.setOpened(true);

            add(confirmDialog);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        initList();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long busId) {
        busResId = busId;

        getBusDetails();
    }
}

