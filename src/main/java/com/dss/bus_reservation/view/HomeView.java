package com.dss.bus_reservation.view;

import com.dss.bus_reservation.entity.User;
import com.dss.bus_reservation.enums.UserRole;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@Route("")
@PermitAll
public class HomeView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthenticationContext authContext;

    public HomeView(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        if (!authContext.isAuthenticated()) {
            event.forwardTo("login");
            return;
        }

        authContext.getAuthenticatedUser(User.class)
                .ifPresent(user -> {

                    if (user.getRole() == UserRole.ADMIN) {
                        event.forwardTo("manage-bus");
                    } else {
                        event.forwardTo("home");
                    }

                });
    }
}
