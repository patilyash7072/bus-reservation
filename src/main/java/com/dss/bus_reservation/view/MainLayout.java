package com.dss.bus_reservation.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.userdetails.UserDetails;


public class MainLayout extends AppLayout {


    private final transient AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;

        H3 logo = new H3("Bus Reservation App");
        logo.addClassName("logo");
        HorizontalLayout
                header =
                authContext.getAuthenticatedUser(UserDetails.class)
                        .map(user -> {
                            Button logout = new Button("Logout", click ->
                                    this.authContext.logout());
                            logout.addClassName(LumoUtility.Margin.Left.AUTO);
                            logout.addClassName(LumoUtility.Margin.Right.SMALL);
                            Span loggedUser = new Span("Welcome " + user.getUsername());
                            return new HorizontalLayout(logo, loggedUser, logout);
                        }).orElseGet(() -> new HorizontalLayout(logo));

        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();

        DrawerToggle toggle = new DrawerToggle();

        SideNav nav = getSideNav();
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");

        Scroller scroller = new Scroller(nav);

        addToDrawer(scroller);

        addToNavbar(toggle, header);

    }

    private SideNav getSideNav() {
        SideNav sideNav = new SideNav();
        sideNav.addItem(
                new SideNavItem("Home", "/", VaadinIcon.HOME.create()),
                new SideNavItem("Passenger", "/passenger",
                        VaadinIcon.USER.create()),
                new SideNavItem("Booking History", "/booking-history",
                        VaadinIcon.TIME_BACKWARD.create()),
                new SideNavItem("Payment History", "/payment-history",
                        VaadinIcon.DOLLAR.create())
        );
        return sideNav;
    }


}
