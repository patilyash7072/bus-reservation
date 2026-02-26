package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.*;
import com.dss.bus_reservation.exception.PassengerAlreadyInitiatedBookingException;
import com.dss.bus_reservation.mapper.NewPassSeatMapper;
import com.dss.bus_reservation.service.BookingService;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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


@Route(value = "new-book", layout = MainLayout.class)
@RolesAllowed("USER")
public class NewBookingView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    Long busResId;

    Dialog dialog;
    Grid<NewPassengerSeatDTO> grid;
    FormLayout form;
    Binder<NewPassengerSeatDTO> binder;

    ComboBox<PassengerDTO> passengerField;
    ComboBox<SeatDTO> seatField;

    Button addButton;
    Button editButton;
    Button deleteButton;
    Button bookButton;

    Set<NewPassengerSeatDTO> passengerSeatDTOList = new HashSet<>();
    Set<PassengerDTO> passengerList = new TreeSet<>();
    Set<SeatDTO> seatList = new TreeSet<>();
    List<PassengerSeatDTO> oldPassList = new ArrayList<>();

    private final transient AuthenticationContext authContext;

    @Resource
    PassengerService passengerService;

    @Resource
    BookingService bookingService;

    @Resource
    SeatService seatService;

    public NewBookingView(AuthenticationContext authContext) {
        this.authContext = authContext;
    }


    @PostConstruct
    public void init() {
        setSizeFull();
        displayToolBar();
        displayGrid();
        initFormFields();
        configureBinder();
    }

    public void displayToolBar() {
        var title = new H3("Book seats");

        addButton = new Button(new Icon(VaadinIcon.PLUS));
        editButton = new Button(new Icon(VaadinIcon.PENCIL));
        deleteButton = new Button(new Icon(VaadinIcon.TRASH));

        bookButton = new Button("Book seats");


        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        addButton.addClickListener(e -> openDialog(false));
        editButton.addClickListener(e -> openDialog(true));
        deleteButton.addClickListener(this::confirmDelete);

        bookButton.addClickListener(e -> {
            try {
                var username = authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get();
                Map<Long, Long> passengerIdMappedBySeatId = new HashMap<>();
                for (NewPassengerSeatDTO dto : passengerSeatDTOList) {
                    passengerIdMappedBySeatId.put(dto.getPassengerDTO().getId(), dto.getSeatDTO().getId());
                }
                var bookingRequest = BookingRequestDTO.builder()
                        .busId(busResId)
                        .username(username)
                        .passengerIdMappedBySeatId(passengerIdMappedBySeatId)
                        .build();
                var bookingResponse = bookingService.bookSeat(bookingRequest);
                updateOldPassList();
                UI.getCurrent().navigate(CheckoutView.class, bookingResponse.getBookingId());

            } catch (PassengerAlreadyInitiatedBookingException ex) {
                Notification notification = new Notification(ex.getMessage(), 3000);
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setOpened(true);
            }
        });

        bookButton.setEnabled(false);

        bookButton.addClassName(LumoUtility.Margin.Left.AUTO);
        bookButton.addClassName(LumoUtility.Margin.Right.MEDIUM);

        var horizontal = new HorizontalLayout(addButton, editButton, deleteButton, bookButton);

        add(title, horizontal);
    }

    public void updateOldPassList() {
        oldPassList = passengerSeatDTOList.stream().map(NewPassSeatMapper::toOld).toList();
        VaadinSession.getCurrent().setAttribute("passList", oldPassList);
    }

    public void initList() {
        var passList = passengerService.findAll();
        if (passList != null) {
            passengerList.addAll(passList);
        }
        var seats = seatService.getAvailableSeats(busResId);
        if (seats != null) {
            seatList.addAll(seats);
        }
    }

    public void displayGrid() {
        grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(e -> e.getPassengerDTO().getName())
                .setHeader("Name")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getPassengerDTO().getName(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Name");

        grid.addColumn(e -> e.getPassengerDTO().getAge())
                .setHeader("Age")
                .setSortable(true)
                .setKey("Age");


        grid.addColumn(e -> e.getPassengerDTO().getGender())
                .setHeader("Gender")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getPassengerDTO().getGender(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Gender");

        grid.addColumn(e -> e.getSeatDTO().getSeatNumber())
                .setHeader("Seat")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getSeatDTO().getSeatNumber(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Seat");


        grid.setItems(passengerSeatDTOList);

        grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent()) {
                editButton.setEnabled(true);
                deleteButton.setEnabled(true);
            } else {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        });

        add(grid);

    }

    public void initFormFields() {


        passengerField = new ComboBox<>("Passenger");
        passengerField.setItems(passengerList);
        passengerField.setItemLabelGenerator(e -> {
            if (e.getName() == null) {
                return "";
            }
            return e.getName();
        });

        seatField = new ComboBox<>("Seat");
        seatField.setItems(seatList);

        seatField.setItemLabelGenerator(e -> {
            if (e.getSeatNumber() == null) {
                return "";
            }
            return e.getSeatNumber();
        });

        passengerField.setPlaceholder("Select Passenger");
        seatField.setPlaceholder("Select Seat");

        passengerField.setClearButtonVisible(true);
        seatField.setClearButtonVisible(true);

        passengerField.clear();
        seatField.clear();


    }

    public void configureBinder() {
        binder = new Binder<>();


        binder.forField(passengerField)
                .asRequired("Passenger cannot be empty")
                .bind(NewPassengerSeatDTO::getPassengerDTO, NewPassengerSeatDTO::setPassengerDTO);

        binder.forField(seatField)
                .asRequired("Seat cannot be empty")
                .bind(NewPassengerSeatDTO::getSeatDTO, NewPassengerSeatDTO::setSeatDTO);
    }


    void openDialog(boolean isEditButtonClicked) {
        var header = isEditButtonClicked ? "Edit Booking" : "Add Booking";
        var dialogTitle = new H3(header);

        form = new FormLayout();

        form.setAutoResponsive(true);
        form.setExpandFields(true);
        form.setColumnWidth("10em");

        var firstRow = form.addFormRow();
        firstRow.add(passengerField);
        firstRow.add(seatField);


        if (isEditButtonClicked) {
            binder.readBean(grid.getSelectionModel().getFirstSelectedItem().get());
        } else {
            binder.readBean(new NewPassengerSeatDTO(new PassengerDTO(), new SeatDTO()));
        }


        var saveButton = new Button("Save", e -> {
            if (binder.validate().isOk()) {
                var newPassengerSeatDTO = new NewPassengerSeatDTO(new PassengerDTO(), new SeatDTO());
                try {
                    binder.writeBean(newPassengerSeatDTO);
                    if (isEditButtonClicked) {
                        passengerSeatDTOList.remove(newPassengerSeatDTO);
                        passengerSeatDTOList.add(newPassengerSeatDTO);
                        grid.getDataProvider().refreshItem(newPassengerSeatDTO);
                        grid.deselectAll();
                        dialog.setOpened(false);
                    } else {
                        if (passengerSeatDTOList.contains(newPassengerSeatDTO)) {
                            Notification.show("Passenger is already mapped to seat. Please edit the existing passenger", 3000, Notification.Position.MIDDLE);
                        } else {
                            passengerSeatDTOList.add(newPassengerSeatDTO);
                            passengerList.remove(newPassengerSeatDTO.getPassengerDTO());
                            seatList.remove(newPassengerSeatDTO.getSeatDTO());
                            grid.getDataProvider().refreshAll();
                            grid.deselectAll();
                            dialog.setOpened(false);
                        }
                    }

                } catch (ValidationException ex) {
                    throw new RuntimeException(ex);
                }
            }
            bookButton.setEnabled(!passengerSeatDTOList.isEmpty());
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

    void confirmDelete(ClickEvent<Button> event) {
        if (grid.getSelectedItems().stream().findFirst().isPresent()) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Delete Seat Booking");
            confirmDialog.setText("Are you sure to delete this seat booking?");
            var deleteButton = new Button("Delete", (e) -> {
                var selected = grid.getSelectionModel().getFirstSelectedItem();
                selected.ifPresent(passSeatDto -> {
                    passengerSeatDTOList.remove(passSeatDto);
                    passengerList.add(passSeatDto.getPassengerDTO());
                    seatList.add(passSeatDto.getSeatDTO());
                    grid.deselectAll();
                    grid.getDataProvider().refreshAll();
                });
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            deleteButton.setClassName(LumoUtility.Background.ERROR);
            confirmDialog.setConfirmButton(deleteButton);
            confirmDialog.setCancelable(true);
            confirmDialog.setOpened(true);

            add(confirmDialog);
        }
        bookButton.setEnabled(!passengerSeatDTOList.isEmpty());
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long busId) {
        busResId = busId;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        initList();
    }
}
