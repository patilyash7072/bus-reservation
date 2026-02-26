package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.PassengerDTO;
import com.dss.bus_reservation.enums.Gender;
import com.dss.bus_reservation.exception.PassengerAlreadyExistsException;
import com.dss.bus_reservation.service.PassengerService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Route(value = "passenger", layout = MainLayout.class)
@PageTitle("Passenger Details")
@RolesAllowed("USER")
public class PassengerView extends VerticalLayout {

    Dialog dialog;
    Grid<PassengerDTO> grid;
    FormLayout form;
    Binder<PassengerDTO> passengerDTOBinder;

    TextField nameField;
    NumberField ageField;
    ComboBox<String> genderField;

    Button addPassengerButton;
    Button editPassengerButton;
    Button deletePassengerButton;


    Set<PassengerDTO> passengerList = new TreeSet<>();


    @Resource
    PassengerService passengerService;

    private final transient AuthenticationContext authContext;

    public PassengerView(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

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
        var title = new H3("Passengers");

        addPassengerButton = new Button(new Icon(VaadinIcon.PLUS));
        editPassengerButton = new Button(new Icon(VaadinIcon.PENCIL));
        deletePassengerButton = new Button(new Icon(VaadinIcon.TRASH));


        editPassengerButton.setEnabled(false);
        deletePassengerButton.setEnabled(false);

        addPassengerButton.addClickListener(e -> openDialog(false));
        editPassengerButton.addClickListener(e -> openDialog(true));
        deletePassengerButton.addClickListener(this::confirmDelete);

        var horizontal = new HorizontalLayout(addPassengerButton, editPassengerButton, deletePassengerButton);

        add(title, horizontal);
    }

    public void initList() {
        var username = authContext.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).get();
        List<PassengerDTO> list = passengerService.findByUsername(username);
        if (list != null) {
            passengerList.addAll(list);
        }
    }

    public void displayGrid() {
        grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(PassengerDTO::getName)
                .setHeader("Name")
                .setSortable(true)
                .setComparator(Comparator.comparing(PassengerDTO::getName, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Name");

        grid.addColumn(PassengerDTO::getAge)
                .setHeader("Age")
                .setSortable(true)
                .setKey("Age");

        grid.addColumn(PassengerDTO::getGender)
                .setHeader("Gender")
                .setSortable(true)
                .setComparator(Comparator.comparing(PassengerDTO::getGender, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .setKey("Gender");

        grid.setItems(passengerList);

        grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent()) {
                editPassengerButton.setEnabled(true);
                deletePassengerButton.setEnabled(true);
            } else {
                editPassengerButton.setEnabled(false);
                deletePassengerButton.setEnabled(false);
            }
        });

        add(grid);

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


    void openDialog(boolean isEditButtonClicked) {
        var header = isEditButtonClicked ? "Edit Passenger Details" : "Add New Passenger";
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

        if (isEditButtonClicked) {
            passengerDTOBinder.readBean(grid.getSelectionModel().getFirstSelectedItem().get());
        } else {
            passengerDTOBinder.readBean(new PassengerDTO());
        }


        var saveButton = new Button("Save", e -> {
            if (passengerDTOBinder.validate().isOk()) {
                var passengerBean = new PassengerDTO();
                try {
                    passengerDTOBinder.writeBean(passengerBean);
                    var passenger = passengerService.find(grid.getSelectionModel().getFirstSelectedItem().orElse(passengerBean));
                    if (passenger != null) {
                        passengerBean.setId(passenger.getId());
                        var updated = passengerService.update(passengerBean);
                        passengerList.remove(updated);
                        passengerList.add(updated);
                        grid.getDataProvider().refreshItem(updated);
                    } else {
                        var saved = passengerService.save(passengerBean);
                        passengerList.add(saved);
                        grid.getDataProvider().refreshAll();
                    }
                    grid.deselectAll();
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

    void confirmDelete(ClickEvent<Button> event) {
        if (grid.getSelectedItems().stream().findFirst().isPresent()) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Delete User");
            confirmDialog.setText("Are you sure to delete this user?");
            var deleteButton = new Button("Delete", (e) -> {
                var selected = grid.getSelectionModel().getFirstSelectedItem();
                selected.ifPresent(passenger -> {
                    passengerService.delete(passenger);
                    passengerList.remove(passenger);
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
