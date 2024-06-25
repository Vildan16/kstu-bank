package kstu.bank.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import kstu.bank.data.User;
import kstu.bank.security.AuthenticatedUser;
import kstu.bank.views.clientcard.ClientCardView;
import kstu.bank.views.credits.AvailableCredits;
import kstu.bank.views.credits.UserCredits;
import kstu.bank.views.support.SupportChatView;
import kstu.bank.views.transaction.AccountGrid;
import kstu.bank.views.transaction.TransactionHistoryView;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Банк КНИТУ");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(ClientCardView.class)) {
            nav.addItem(new SideNavItem("Личный кабинет", ClientCardView.class, LineAwesomeIcon.USER.create()));

        }
        if (accessChecker.hasAccess(TransactionHistoryView.class)) {
            nav.addItem(
                    new SideNavItem("История операций", TransactionHistoryView.class, LineAwesomeIcon.TH_SOLID.create()));

        }
        if (accessChecker.hasAccess(AccountGrid.class)) {
            nav.addItem(new SideNavItem("Опись счетов", AccountGrid.class, LineAwesomeIcon.MONEY_BILL_ALT_SOLID.create()));
        }
        if (accessChecker.hasAccess(TransferEditForm.class)) {
            nav.addItem(new SideNavItem("Перевод средств", TransferEditForm.class, LineAwesomeIcon.MONEY_BILL_WAVE_ALT_SOLID.create()));
        }
        if (accessChecker.hasAccess(AvailableCredits.class)) {
            nav.addItem(new SideNavItem("Доступные кредиты", AvailableCredits.class, LineAwesomeIcon.MONEY_BILL_ALT_SOLID.create()));
        }
        if (accessChecker.hasAccess(UserCredits.class)) {
            nav.addItem(new SideNavItem("Мои кредиты", UserCredits.class, LineAwesomeIcon.MONEY_BILL_ALT_SOLID.create()));
        }
        if (accessChecker.hasAccess(SupportChatView.class)) {
            nav.addItem(new SideNavItem("Техподдержка", SupportChatView.class, LineAwesomeIcon.COMMENTS.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName() + " " + user.getSurname());
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Выйти", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Войти");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
