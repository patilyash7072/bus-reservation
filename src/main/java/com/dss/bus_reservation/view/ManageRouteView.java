package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.RouteDTO;
import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.exception.RouteAlreadyExistsException;
import com.dss.bus_reservation.service.RouteService;
import com.dss.bus_reservation.service.StationService;
import com.vaadin.flow.component.ClickEvent;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;

import java.util.*;

@Route(value = "manage-route", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class ManageRouteView extends VerticalLayout {


    Dialog dialog;
    Grid<RouteDTO> grid;
    FormLayout form;
    Binder<RouteDTO> routeDTOBinder;

    NumberField totalDistanceField;
    ComboBox<StationDTO> sourceField;
    ComboBox<StationDTO> destinationField;

    Set<StationDTO> stationList = new TreeSet<>();
    StationDTO removedSource;
    StationDTO removedDestination;


    Button addRouteButton;
    Button editRouteButton;
    Button deleteRouteButton;


    Set<RouteDTO> routeDTOList = new TreeSet<>();


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
        var title = new H3("Routes");

        addRouteButton = new Button(new Icon(VaadinIcon.PLUS));
        editRouteButton = new Button(new Icon(VaadinIcon.PENCIL));
        deleteRouteButton = new Button(new Icon(VaadinIcon.TRASH));


        editRouteButton.setEnabled(false);
        deleteRouteButton.setEnabled(false);

        addRouteButton.addClickListener(e -> openDialog(false));
        editRouteButton.addClickListener(e -> openDialog(true));
        deleteRouteButton.addClickListener(this::confirmDelete);

        var horizontal = new HorizontalLayout(addRouteButton, editRouteButton, deleteRouteButton);

        add(title, horizontal);
    }

    public void initList() {
        List<RouteDTO> list = routeService.findAll();
        if (list != null) {
            routeDTOList.addAll(list);
        }
    }

    public void displayGrid() {
        grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(e -> e.getSource().getName())
                .setHeader("Source")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getSource().getName(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Source");

        grid.addColumn(e -> e.getDestination().getName())
                .setHeader("Destination")
                .setSortable(true)
                .setComparator(Comparator.comparing(e -> e.getDestination().getName(), Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Destination");

        grid.addColumn(RouteDTO::getTotalDistance)
                .setHeader("Total Distance")
                .setSortable(true)
                .setKey("Total Distance");

        grid.setItems(routeDTOList);

        grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent()) {
                editRouteButton.setEnabled(true);
                deleteRouteButton.setEnabled(true);
            } else {
                editRouteButton.setEnabled(false);
                deleteRouteButton.setEnabled(false);
            }
        });

        add(grid);

    }

    public void initFormFields() {
        stationList.addAll(stationService.findAll());
        sourceField = new ComboBox<>("Source");
        sourceField.setItems(stationList);
        sourceField.setItemLabelGenerator(StationDTO::getName);

        destinationField = new ComboBox<>("Destination");
        destinationField.setItems(stationList);
        destinationField.setItemLabelGenerator(StationDTO::getName);

        sourceField.setPlaceholder("Select Source");
        destinationField.setPlaceholder("Select Destination");

        sourceField.setClearButtonVisible(true);
        destinationField.setClearButtonVisible(true);

        totalDistanceField = new NumberField("Total Distance");


        sourceField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                stationList.remove(event.getValue());
                if (removedSource != null) {
                    stationList.add(removedSource);
                }
                removedSource = event.getValue();
                sourceField.getDataProvider().refreshAll();
                destinationField.getDataProvider().refreshAll();
            } else {
                if (removedSource != null) {
                    stationList.add(removedSource);
                    removedSource = null;
                    sourceField.getDataProvider().refreshAll();
                    destinationField.getDataProvider().refreshAll();
                }
            }
        });

        destinationField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                stationList.remove(event.getValue());
                if (removedDestination != null) {
                    stationList.add(removedDestination);
                }
                removedDestination = event.getValue();
                sourceField.getDataProvider().refreshAll();
                destinationField.getDataProvider().refreshAll();
            } else {
                if (removedDestination != null) {
                    stationList.add(removedDestination);
                    removedDestination = null;
                    sourceField.getDataProvider().refreshAll();
                    destinationField.getDataProvider().refreshAll();
                }
            }
        });

    }

    public void configureBinder() {
        routeDTOBinder = new Binder<>();


        routeDTOBinder.forField(sourceField)
                .asRequired("Source cannot be empty")
                .bind(RouteDTO::getSource, RouteDTO::setSource);

        routeDTOBinder.forField(destinationField)
                .asRequired("Destination cannot be empty")
                .bind(RouteDTO::getDestination, RouteDTO::setDestination);


        routeDTOBinder.forField(totalDistanceField)
                .asRequired("Distance cannot be empty")
                .withValidator(e -> e > 0, "Distance cannot be zero or negative")
                .withConverter(Double::intValue, integer -> {
                    if (integer != null) {
                        return integer.doubleValue();
                    }
                    return Double.parseDouble("1");
                })
                .bind(RouteDTO::getTotalDistance, RouteDTO::setTotalDistance);
    }


    void openDialog(boolean isEditButtonClicked) {
        var header = isEditButtonClicked ? "Edit Route Details" : "Add New Route";
        var dialogTitle = new H3(header);

        form = new FormLayout();

        form.setAutoResponsive(true);
        form.setExpandFields(true);
        form.setColumnWidth("10em");

        var firstRow = form.addFormRow();
        firstRow.add(sourceField);
        firstRow.add(destinationField);

        var secondRow = form.addFormRow();
        secondRow.add(totalDistanceField);


        if (isEditButtonClicked) {
            routeDTOBinder.readBean(grid.getSelectionModel().getFirstSelectedItem().get());
        } else {
            routeDTOBinder.readBean(new RouteDTO());
        }


        var saveButton = new Button("Save", e -> {
            if (routeDTOBinder.validate().isOk()) {

                var routeDTO = new RouteDTO();
                try {
                    routeDTOBinder.writeBean(routeDTO);
                    var route = routeService.find(grid.getSelectionModel().getFirstSelectedItem().orElse(routeDTO));
                    if (route != null) {
                        routeDTO.setId(route.getId());
                        var updated = routeService.update(routeDTO);
                        routeDTOList.remove(updated);
                        routeDTOList.add(updated);
                        grid.getDataProvider().refreshItem(updated);
                    } else {
                        var saved = routeService.add(routeDTO);
                        routeDTOList.add(saved);
                        grid.getDataProvider().refreshAll();
                    }
                    grid.deselectAll();
                    dialog.setOpened(false);
                } catch (RouteAlreadyExistsException ex) {
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
                selected.ifPresent(routeDTO -> {
                    routeService.delete(routeDTO);
                    routeDTOList.remove(routeDTO);
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
