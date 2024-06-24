package kstu.bank.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import kstu.bank.security.AuthenticatedUser;

@AnonymousAllowed
@PageTitle("Вход")
@Route(value = "login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final LoginOverlay loginOverlay;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        this.loginOverlay = new LoginOverlay();

        loginOverlay.setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Банк КНИТУ");
        i18n.getHeader().setDescription("Авторизация");
        i18n.getForm().setTitle("Вход");
        i18n.getForm().setUsername("Логин");
        i18n.getForm().setPassword("Пароль");
        i18n.getErrorMessage().setUsername("Введите логин");
        i18n.getErrorMessage().setPassword("Введите пароль");
        i18n.getForm().setSubmit("Войти");
        i18n.getErrorMessage().setTitle("Пользователь не найден");
        i18n.getErrorMessage().setMessage("Пользователь с такими данными не найден. Перед началом использования зарегистрируйтесь");
        i18n.getForm().setForgotPassword("Регистрация");
        i18n.setAdditionalInformation(null);
        loginOverlay.setI18n(i18n);
        loginOverlay.addForgotPasswordListener(e -> {
            loginOverlay.close();
            UI.getCurrent().navigate(RegistrationView.class);
        });

        loginOverlay.setForgotPasswordButtonVisible(true);
        loginOverlay.setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            loginOverlay.setOpened(false);
            event.forwardTo("");
        }

        loginOverlay.setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
