package com.dss.bus_reservation.view;

import com.dss.bus_reservation.dto.NewUserDTO;
import com.dss.bus_reservation.enums.UserRole;
import com.dss.bus_reservation.exception.UserAlreadyRegisteredException;
import com.dss.bus_reservation.service.UserService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Route("/register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    H1 header;
    H3 title;

    TextField username;
    PasswordField password;
    ComboBox<UserRole> role;

    Binder<NewUserDTO> binder;

    Button registerButton;

    Anchor loginLink;

    @Resource
    private UserService userService;

    @PostConstruct
    public void init() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        initFields();
        configureBinder();
        displayFields();
    }

    public void initFields() {
        header = new H1("Bus Reservation Application");
        title = new H3("Register User");
        username = new TextField("Username");
        password = new PasswordField("Password");

        role = new ComboBox<>("Role");
        role.setItems(UserRole.values());
        role.setItemLabelGenerator(Enum::name);

        username.setPlaceholder("Enter username");
        password.setPlaceholder("Enter password");
        role.setPlaceholder("Select role");

        registerButton = new Button("Register", this::register);
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        loginLink = new Anchor("/login", "Already registered? Login here");

        binder = new Binder<>();
    }
    public void configureBinder() {

        String regex = "^[a-zA-Z][a-zA-Z0-9_]{2,15}$";
        binder.forField(username)
                .asRequired("Username is required")
                .withValidator(u -> u.length() >= 6, "Username should contain minimum 6 characters")
                .withValidator(u -> u.length() <= 10, "Username should not exceed 10 characters")
                .withValidator(u -> u.matches(regex), "Username contains invalid characters. Only letters, numbers, dots and underscores are allowed")
                .bind(NewUserDTO::getUsername, NewUserDTO::setUsername);

        binder.forField(password)
                .asRequired("Password is required")
                .withValidator(p -> p.length() >= 6, "Password should contain minimum 6 characters")
                .withValidator(p -> p.length() <= 20, "Password should not be greater than 20 characters")
                .bind(NewUserDTO::getPassword, NewUserDTO::setPassword);

        binder.forField(role)
                .asRequired("Role is required")
                .bind(NewUserDTO::getRole, NewUserDTO::setRole);
    }


    public void register(ClickEvent<Button> event) {
        try {
            if (binder.validate().isOk()) {
                userService.create(username.getValue(), password.getValue(), role.getValue());
                UI.getCurrent().navigate(LoginView.class);
            }
        } catch (UserAlreadyRegisteredException e) {
            Notification notification = new Notification(e.getMessage(), 3000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            notification.setOpened(true);
        }
    }

    public void displayFields() {
        add(header, title, username, password, role, registerButton, loginLink);
    }
}
