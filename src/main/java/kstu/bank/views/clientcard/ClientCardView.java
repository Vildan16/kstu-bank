package kstu.bank.views.clientcard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import kstu.bank.components.avataritem.AvatarItem;
import kstu.bank.data.User;
import kstu.bank.data.UserRepository;
import kstu.bank.security.AuthenticatedUser;
import kstu.bank.services.CrmService;
import kstu.bank.views.MainLayout;

@PageTitle("Личный кабинет")
@Route(value = "card", layout = MainLayout.class)
@AnonymousAllowed
public class ClientCardView extends Composite<VerticalLayout> {
    private final AuthenticatedUser authenticatedUser;
    private final UserRepository userRepository;
    private final CrmService crmService;
    private final TextField nameField;
    private final TextField surnameField;
    private final DatePicker birthDate;
    private final TextField phoneNumber;
    private final EmailField emailField;

    public ClientCardView(AuthenticatedUser authenticatedUser, UserRepository userRepository, CrmService crmService) {
        this.authenticatedUser = authenticatedUser;
        this.userRepository = userRepository;
        this.crmService = crmService;
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h3 = new H3();
        H4 h4 = new H4();
        h4.setText("Баланс: " + crmService.getBalance(authenticatedUser.get().get()) + " р.");
        h4.setWidth("100%");

        AvatarItem avatarItem = new AvatarItem();
        FormLayout formLayout2Col = new FormLayout();
        nameField = new TextField();
        surnameField = new TextField();
        birthDate = new DatePicker();
        phoneNumber = new TextField();
        emailField = new EmailField();
        HorizontalLayout layoutRow = new HorizontalLayout();
        Button buttonPrimary = new Button();
        Button buttonSecondary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        layoutColumn2.setWidth("100%");
        layoutColumn2.setMaxWidth("800px");
        layoutColumn2.setHeight("min-content");
        h3.setText("Персональные данные");
        h3.setWidth("100%");
        avatarItem.setWidth("min-content");
        setAvatarItemSampleData(avatarItem);
        formLayout2Col.setWidth("100%");
        nameField.setLabel("Имя");
        surnameField.setLabel("Фамилия");
        birthDate.setLabel("Дата рождения");
        phoneNumber.setLabel("Номер телефона");
        emailField.setLabel("Email");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        buttonPrimary.setText("Сохранить");
        buttonPrimary.addClickListener(this::saveData);
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonSecondary.setText("Отмена");
        buttonSecondary.addClickListener(e -> setValues());
        buttonSecondary.setWidth("min-content");
        getContent().add(layoutColumn2);
        layoutColumn2.add(h4);
        layoutColumn2.add(h3);
        layoutColumn2.add(avatarItem);
        layoutColumn2.add(formLayout2Col);
        formLayout2Col.add(nameField);
        formLayout2Col.add(surnameField);
        formLayout2Col.add(birthDate);
        formLayout2Col.add(phoneNumber);
        formLayout2Col.add(emailField);
        layoutColumn2.add(layoutRow);
        layoutRow.add(buttonPrimary);
        layoutRow.add(buttonSecondary);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setValues();
    }

    private void setValues() {
        User user = authenticatedUser.get().get();

        nameField.setValue(user.getName() != null ? user.getName() : "");
        surnameField.setValue(user.getSurname() != null ? user.getSurname() : "");
        birthDate.setValue(user.getBirthDate());
        phoneNumber.setValue(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        emailField.setValue(user.getEmail() != null ? user.getEmail() : "");
    }

    private void saveData(ClickEvent<Button> buttonClickEvent) {
        User user = authenticatedUser.get().get();

        user.setName(nameField.getValue());
        user.setSurname(surnameField.getValue());
        user.setBirthDate(birthDate.getValue());
        user.setPhoneNumber(phoneNumber.getValue());
        user.setEmail(emailField.getValue());
        userRepository.save(user);
        Notification.show("Данные сохранены");
    }

    private void setAvatarItemSampleData(AvatarItem avatarItem) {
        avatarItem.setHeading(authenticatedUser.get().get().getName() + " " + authenticatedUser.get().get().getSurname());
        avatarItem.setDescription(null);
        avatarItem.setAvatar(new Avatar(authenticatedUser.get().get().getName() + " " + authenticatedUser.get().get().getSurname()));
    }
}
