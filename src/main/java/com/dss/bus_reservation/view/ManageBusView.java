package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.AdminBusReservationDTO;
import com.dss.bus_reservation.dto.RouteDTO;
import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.enums.BusType;
import com.dss.bus_reservation.exception.BusAlreadyExistsException;
import com.dss.bus_reservation.exception.InvalidRouteException;
import com.dss.bus_reservation.exception.RouteAlreadyExistsException;
import com.dss.bus_reservation.service.BusReservationService;
import com.dss.bus_reservation.service.RouteService;
import com.dss.bus_reservation.service.StationService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.*;

@Route(value = "manage-bus", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class ManageBusView extends VerticalLayout {


    Dialog dialog;
    Grid<AdminBusReservationDTO> grid;
    FormLayout form;
    Binder<AdminBusReservationDTO> adminBusReservationDTOBinder;


    ComboBox<RouteDTO> routeField;
    ComboBox<StationDTO> sourceField;
    ComboBox<StationDTO> destinationField;
    NumberField totalDistanceField;
    ComboBox<BusType> busTypeField;
    TimePicker startTimeField;
    TimePicker endTimeField;
    DatePicker dateOfJourneyField;


    Button addButton;
    Button editButton;
    Button deleteButton;


    Set<AdminBusReservationDTO> busReservationDTOList = new TreeSet<>();


    @Resource
    BusReservationService busReservationService;

    @Resource
    RouteService routeService;

    @Resource
    StationService stationService;


    @PostConstruct
    public void init() {
        setSizeFull();
        displayToolBar();
        initList();
        displayGrid();
        initFormFields();
        configureBinder();
    }

    public void displayToolBar() {
        var title = new H3("Buses");

        addButton = new Button(new Icon(VaadinIcon.PLUS));
        editButton = new Button(new Icon(VaadinIcon.PENCIL));
        deleteButton = new Button(new Icon(VaadinIcon.TRASH));


        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        addButton.addClickListener(e -> openDialog(false));
        editButton.addClickListener(e -> openDialog(true));
        deleteButton.addClickListener(this::confirmDelete);

        var horizontal = new HorizontalLayout(addButton, editButton, deleteButton);

        add(title, horizontal);
    }

    public void initList() {
        List<AdminBusReservationDTO> list = busReservationService.findAll();
        if (list != null) {
            busReservationDTOList.addAll(list);
        }
    }

    public void displayGrid() {
        grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(e -> e.getRouteDTO().getSource().getName())
                .setHeader("Source")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getRouteDTO().getSource().getName(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Source");

        grid.addColumn(e -> e.getRouteDTO().getDestination().getName())
                .setHeader("Destination")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getRouteDTO().getDestination().getName(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Destination");

        grid.addColumn(e -> e.getRouteDTO().getTotalDistance())
                .setHeader("Total Distance")
                .setSortable(true)
                .setKey("Total Distance");

        grid.addColumn(AdminBusReservationDTO::getStartTime)
                .setHeader("Start Time")
                .setSortable(true)
                .setKey("Start Time");

        grid.addColumn(AdminBusReservationDTO::getEndTime)
                .setHeader("End Time")
                .setSortable(true)
                .setKey("End Time");

        grid.addColumn(AdminBusReservationDTO::getDateOfJourney)
                .setHeader("Date of Journey")
                .setSortable(true)
                .setKey("Date of Journey");

        grid.addColumn(AdminBusReservationDTO::getType)
                .setHeader("Bus Type")
                .setSortable(true)
                .setKey("Bus Type");

        grid.setItems(busReservationDTOList);

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
        routeField = new ComboBox<>("Route");
        var routeList = routeService.findAll();
        if (routeList != null) {
            routeField.setItems(routeList);
        } else {
            routeField.setItems(new ArrayList<>());
        }
        routeField.setItemLabelGenerator(e -> {
            if (e.getId() == null) {
                return "";
            }
            return e.getId() + " (" + e.getSource().getName() + " --- " +  e.getDestination().getName() + ")";
        });



        sourceField = new ComboBox<>("Source");
        destinationField = new ComboBox<>("Destination");
        totalDistanceField = new NumberField("Total Distance (km)");

        sourceField.setReadOnly(true);
        destinationField.setReadOnly(true);
        totalDistanceField.setReadOnly(true);


        sourceField.setItems(stationService.findAll());
        destinationField.setItems(stationService.findAll());


        sourceField.setItemLabelGenerator(e -> {
            if (e.getName() == null) {
                return "";
            }
            return e.getName();
        });

        destinationField.setItemLabelGenerator(e -> {
            if (e.getName() == null) {
                return "";
            }
            return e.getName();
        });

        startTimeField = new TimePicker("Start Time");
        endTimeField = new TimePicker("End Time");
        dateOfJourneyField = new DatePicker("Date of Journey");

        busTypeField = new ComboBox<>("Bus Type");
        busTypeField.setItems(Arrays.stream(BusType.values()).toList());
        busTypeField.setItemLabelGenerator(BusType::name);

        routeField.setPlaceholder("Select Route Number");
        sourceField.setPlaceholder("Source");
        destinationField.setPlaceholder("Destination");
        totalDistanceField.setPlaceholder("Total Distance (km)");
        startTimeField.setPlaceholder("Select Start Time");
        endTimeField.setPlaceholder("Select End Time");
        dateOfJourneyField.setPlaceholder("Select Date of Journey");
        routeField.setPlaceholder("Select Route");
        busTypeField.setPlaceholder("Select Bus Type");

        routeField.setClearButtonVisible(true);
        dateOfJourneyField.setClearButtonVisible(true);
        busTypeField.setClearButtonVisible(true);

        startTimeField.setClearButtonVisible(true);
        endTimeField.setClearButtonVisible(true);

        dateOfJourneyField.setInitialPosition(LocalDate.now());
        dateOfJourneyField.setMin(LocalDate.now());
        dateOfJourneyField.setValue(LocalDate.now());
        dateOfJourneyField.setMax(LocalDate.now().plusMonths(3L).withDayOfMonth(31));

        routeField.addValueChangeListener(e -> {
                RouteDTO selectedRoute = e.getValue();
                if (selectedRoute != null) {
                    if (selectedRoute.getSource() != null) sourceField.setValue(selectedRoute.getSource());
                    if (selectedRoute.getDestination() != null) destinationField.setValue(selectedRoute.getDestination());
                    if (selectedRoute.getTotalDistance() != null) totalDistanceField.setValue(selectedRoute.getTotalDistance().doubleValue());
                } else {
                    sourceField.clear();
                    destinationField.clear();
                    totalDistanceField.clear();
                }
        });
    }

    public void configureBinder() {
        adminBusReservationDTOBinder = new Binder<>();


        adminBusReservationDTOBinder.forField(routeField)
                .asRequired("Route cannot be empty")
                .bind(AdminBusReservationDTO::getRouteDTO, AdminBusReservationDTO::setRouteDTO);

        adminBusReservationDTOBinder.forField(startTimeField)
                .asRequired("Start Time cannot be empty")
                        .bind(AdminBusReservationDTO::getStartTime, AdminBusReservationDTO::setStartTime);

        adminBusReservationDTOBinder.forField(endTimeField)
                .asRequired("End Time cannot be empty")
                .bind(AdminBusReservationDTO::getEndTime, AdminBusReservationDTO::setEndTime);

        adminBusReservationDTOBinder.forField(dateOfJourneyField)
                .asRequired("Date of Journey cannot be empty")
                .bind(AdminBusReservationDTO::getDateOfJourney, AdminBusReservationDTO::setDateOfJourney);

        adminBusReservationDTOBinder.forField(busTypeField)
                .asRequired("Bus Type cannot be empty")
                .bind(AdminBusReservationDTO::getType, AdminBusReservationDTO::setType);


        adminBusReservationDTOBinder.forField(totalDistanceField)
                .withConverter(
//                        Double::intValue, integer -> {
//                    if (integer != null) {
//                        return integer.doubleValue();
//                    }
//                    return Double.parseDouble("0");
//                }

                        doubleValue -> doubleValue == null ? null : doubleValue.intValue(),
                        integerValue -> integerValue == null ? null : integerValue.doubleValue(),
                        "Total Distance must be a number"
                )
                .bind(e -> e.getRouteDTO().getTotalDistance(), (e,v) -> e.getRouteDTO().setTotalDistance(v));
    }


    void openDialog(boolean isEditButtonClicked) {
        var header = isEditButtonClicked ? "Edit Bus Details" : "Add New Bus";
        var dialogTitle = new H3(header);

        form = new FormLayout();

        form.setAutoResponsive(true);
        form.setExpandFields(true);
        form.setColumnWidth("18em");

        var firstRow = form.addFormRow();
        firstRow.add(routeField);
        firstRow.add(sourceField);

        var secondRow = form.addFormRow();
        secondRow.add(destinationField);
        secondRow.add(totalDistanceField);

        var thirdRow = form.addFormRow();
        thirdRow.add(startTimeField);
        thirdRow.add(endTimeField);

        var fourthRow = form.addFormRow();
        fourthRow.add(dateOfJourneyField);
        fourthRow.add(busTypeField);


        if (isEditButtonClicked) {
            adminBusReservationDTOBinder.readBean(grid.getSelectionModel().getFirstSelectedItem().get());
        } else {
            var busDTO = new AdminBusReservationDTO();
            RouteDTO routeDTO = new RouteDTO();
            var sourceDTO = new StationDTO();
            var destinationDTO = new StationDTO();
            routeDTO.setSource(sourceDTO);
            routeDTO.setDestination(destinationDTO);
            busDTO.setRouteDTO(routeDTO);
            adminBusReservationDTOBinder.readBean(busDTO);
        }


        var saveButton = new Button("Save", e -> {
            if (adminBusReservationDTOBinder.validate().isOk()) {

                var busDTO = new AdminBusReservationDTO();
                RouteDTO routeDTO = new RouteDTO();
                var sourceDTO = new StationDTO();
                var destinationDTO = new StationDTO();
                routeDTO.setSource(sourceDTO);
                routeDTO.setDestination(destinationDTO);
                busDTO.setRouteDTO(routeDTO);
                try {
                    adminBusReservationDTOBinder.writeBean(busDTO);
                    if (busDTO.getRouteDTO().getId() == null) {
                        throw new InvalidRouteException("Route in invalid. Please select a route");
                    }
                    var bus = busReservationService.find(grid.getSelectionModel().getFirstSelectedItem().orElse(busDTO));
                    if (bus != null) {
                        busDTO.setId(bus.getId());
                        var updated = busReservationService.update(busDTO);
                        busReservationDTOList.remove(updated);
                        busReservationDTOList.add(updated);
                        grid.getDataProvider().refreshItem(updated);
                    } else {
                        var saved = busReservationService.add(busDTO);
                        busReservationDTOList.add(saved);
                        grid.getDataProvider().refreshAll();
                    }
                    grid.deselectAll();
                    dialog.setOpened(false);
                } catch (BusAlreadyExistsException | InvalidRouteException ex) {
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

    void confirmDelete(ClickEvent<Button> event) {
        if (grid.getSelectedItems().stream().findFirst().isPresent()) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Delete Route");
            confirmDialog.setText("Are you sure to delete this route?");
            var deleteButton = new Button("Delete", (e) -> {
                var selected = grid.getSelectionModel().getFirstSelectedItem();
                selected.ifPresent(dto -> {
                    busReservationService.delete(dto);
                    busReservationDTOList.remove(dto);
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
    }

}
