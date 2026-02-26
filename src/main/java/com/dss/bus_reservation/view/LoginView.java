package com.dss.bus_reservation.view;

import com.dss.bus_reservation.entity.User;
import com.dss.bus_reservation.enums.UserRole;
import com.dss.bus_reservation.service.MasterService;
import com.dss.bus_reservation.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Route("login")
@PageTitle("Login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    @Resource
    UserService userService;

    private final transient AuthenticationContext authContext;

    private LoginForm login = new LoginForm();

    public LoginView(AuthenticationContext authContext) {
        this.authContext = authContext;
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.setAction("login");

        var registerButton = new Button("Register", e-> UI.getCurrent().navigate(RegistrationView.class));

        add(new H1("Bus Reservation Application"), login, registerButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
        Optional<User> user = authContext.getAuthenticatedUser(User.class);
        if (user.isPresent()) {
            beforeEnterEvent.forwardTo(HomeView.class);
        }
    }
}
