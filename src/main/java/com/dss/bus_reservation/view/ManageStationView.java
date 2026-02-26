package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.StationDTO;
import com.dss.bus_reservation.exception.StationAlreadyExistsException;
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
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Route(value = "manage-station", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class ManageStationView extends VerticalLayout {


    Dialog dialog;
    Grid<StationDTO> grid;
    FormLayout form;
    Binder<StationDTO> StationDTOBinder;

    TextField stationField;

    Button addButton;
    Button editButton;
    Button deleteButton;


    Set<StationDTO> StationDTOList = new TreeSet<>();


    @Resource
    StationService StationService;

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
        var title = new H3("Stations");

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
        List<StationDTO> list = StationService.findAll();
        if (list != null) {
            StationDTOList.addAll(list);
        }
    }

    public void displayGrid() {
        grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(StationDTO::getName).setHeader("Station Name").setSortable(true).setComparator(Comparator.comparing(StationDTO::getName, Comparator.nullsFirst(String::compareToIgnoreCase))).setKey("Station Name");

        grid.setItems(StationDTOList);

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
        stationField = new TextField("Station Name");
        stationField.setPlaceholder("Enter Station Name");
        stationField.setAutocomplete(Autocomplete.OFF);

    }

    public void configureBinder() {
        StationDTOBinder = new Binder<>();


        StationDTOBinder.forField(stationField)
                .asRequired("Station cannot be empty")
                .withValidator(stn -> stn.matches("^[A-Za-z]+( [A-Za-z]+)*$"), "Invalid station name")
                .withValidator(stn -> stn.length() < 15, " Length of station cannot be greater than 15")
                .bind(StationDTO::getName, StationDTO::setName);
    }


    void openDialog(boolean isEditButtonClicked) {
        var header = isEditButtonClicked ? "Edit Station Name" : "Add New Station";
        var dialogTitle = new H3(header);

        form = new FormLayout();

        form.add(stationField);

        if (isEditButtonClicked) {
            StationDTOBinder.readBean(grid.getSelectionModel().getFirstSelectedItem().get());
        } else {
            StationDTOBinder.readBean(new StationDTO());
        }


        var saveButton = new Button("Save", e -> {
            if (StationDTOBinder.validate().isOk()) {

                var StationDTO = new StationDTO();
                try {
                    StationDTOBinder.writeBean(StationDTO);
                    var Station = StationService.find(grid.getSelectionModel().getFirstSelectedItem().orElse(StationDTO));
                    if (Station != null) {
                        StationDTO.setId(Station.getId());
                        var updated = StationService.update(StationDTO);
                        StationDTOList.remove(StationDTO);
                        StationDTOList.add(updated);
                        grid.getDataProvider().refreshItem(updated);
                    } else {
                        var saved = StationService.add(StationDTO);
                        StationDTOList.add(saved);
                        grid.getDataProvider().refreshAll();
                    }
                    grid.deselectAll();
                    dialog.setOpened(false);
                } catch (StationAlreadyExistsException ex) {
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
            confirmDialog.setHeader("Delete Station");
            confirmDialog.setText("Are you sure to delete this Station?");
            var deleteButton = new Button("Delete", (e) -> {
                var selected = grid.getSelectionModel().getFirstSelectedItem();
                selected.ifPresent(StationDTO -> {
                    StationService.delete(StationDTO);
                    StationDTOList.remove(StationDTO);
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
