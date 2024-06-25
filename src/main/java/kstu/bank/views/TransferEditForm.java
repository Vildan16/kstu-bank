package kstu.bank.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityNotFoundException;
import kstu.bank.components.avataritem.AvatarItem;
import kstu.bank.data.Account;
import kstu.bank.data.Transaction;
import kstu.bank.data.User;
import kstu.bank.data.UserRepository;
import kstu.bank.security.AuthenticatedUser;
import kstu.bank.services.CrmService;
import kstu.bank.views.MainLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Перевод средств")
@Route(value = "transfer-edit", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
public class TransferEditForm extends Composite<VerticalLayout> {
    private final AuthenticatedUser authenticatedUser;
    private final UserRepository userRepository;
    private final CrmService crmService;
    private final ComboBox<Account> fromAccField;
    private final TextField toAccField;
    private final TextField sum;
    private final H4 h4;
    private final Button sendButton;

    public TransferEditForm(AuthenticatedUser authenticatedUser, UserRepository userRepository, CrmService crmService) {
        this.authenticatedUser = authenticatedUser;
        this.userRepository = userRepository;
        this.crmService = crmService;
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h3 = new H3();
        h4 = new H4();
        h4.setText("Доступно для перевода: " + 0 + " р.");
        h4.setWidth("100%");

        AvatarItem avatarItem = new AvatarItem();
        FormLayout formLayout2Col = new FormLayout();
        fromAccField = new ComboBox("Счет списания", crmService.getAllAccounts(authenticatedUser.get().get()).stream().filter(a -> !"Закрыт".equals(a.getStatus())).collect(Collectors.toSet()));
        fromAccField.setItemLabelGenerator(Account::getAccountNumber);
        fromAccField.setRequired(true);
        toAccField = new TextField();
        toAccField.setMaxLength(20);
        fromAccField.addValueChangeListener(e -> setaval());
        toAccField.addValueChangeListener(e -> setaval());
        sum = new TextField("Сумма");
        sum.addValueChangeListener(e -> setaval());
        sum.setValue("0");
        sum.setAllowedCharPattern("\\d");
        HorizontalLayout layoutRow = new HorizontalLayout();
        sendButton = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        layoutColumn2.setWidth("100%");
        layoutColumn2.setMaxWidth("800px");
        layoutColumn2.setHeight("min-content");
        h3.setText("Перевод средств");
        h3.setWidth("100%");
        avatarItem.setWidth("min-content");
        setAvatarItemSampleData(avatarItem);
        formLayout2Col.setWidth("100%");
        toAccField.setLabel("Счет получателя");
        toAccField.setAllowedCharPattern("\\d");
        toAccField.setErrorMessage("Введите 20-значное значение");
        toAccField.setMaxLength(20);
        toAccField.setMinLength(20);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        sendButton.setText("Выполнить перевод");
        sendButton.setEnabled(false);
        sendButton.addClickListener(this::saveData);
        sendButton.setWidth("min-content");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutColumn2);
        layoutColumn2.add(h3);
        layoutColumn2.add(avatarItem);
        layoutColumn2.add(formLayout2Col);
        formLayout2Col.add(fromAccField);
        formLayout2Col.add(toAccField);
        formLayout2Col.add(sum);
        layoutColumn2.add(layoutRow);
        layoutRow.add(sendButton);
        fromAccField.addValueChangeListener(e -> h4.setText("Доступно для перевода: " + (fromAccField.getValue() == null ? "0" : fromAccField.getValue().getBalance()) + " р."));
        layoutColumn2.add(h4);
    }

    private void setaval() {
        if (fromAccField.getValue() != null && toAccField.getValue() != null && !"0".equals(sum.getValue())) {
            sendButton.setEnabled(true);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setValues();
    }

    private void setValues() {
        User user = authenticatedUser.get().get();
        h4.setText("Доступно для перевода: " + 0 + " р.");
        fromAccField.clear();
        toAccField.clear();
        sum.setValue("0");
    }

    private void saveData(ClickEvent<Button> buttonClickEvent) {
        User user = authenticatedUser.get().get();
        if (fromAccField.getValue() == null || toAccField.getValue() == null || sum.getValue() == null || Long.parseLong(sum.getValue()) < 1) {
            return;
        }
        try {
            Account accountFrom = crmService.getAccount(fromAccField.getValue().getAccountNumber());
            Account accountTo = crmService.getAccount(String.valueOf(toAccField.getValue()));
            if (accountFrom.getBalance() < Long.parseLong(sum.getValue())) {
                Notification.show("Недостаточно средств!").setPosition(Notification.Position.MIDDLE);
                return;
            }
            User userTo = accountTo.getUser();
            accountFrom.setBalance(accountFrom.getBalance() - Long.parseLong(sum.getValue()));
            accountTo.setBalance(accountTo.getBalance() + Long.parseLong(sum.getValue()));
            Transaction transaction = new Transaction();
            transaction.setFrom(user);
            transaction.setTo(userTo);
            transaction.setStatus("Выполнен");
            transaction.setSum(Long.parseLong(sum.getValue()));
            transaction.setDateTime(LocalDateTime.now());
            crmService.saveTransaction(transaction, accountFrom, accountTo);
            setValues();
            Notification.show("Перевод выполнен");
        } catch (Exception e) {
            Notification.show("Пользователь с таким счетом не найден");
        }

    }

    private void setAvatarItemSampleData(AvatarItem avatarItem) {
        avatarItem.setHeading(authenticatedUser.get().get().getName() + " " + authenticatedUser.get().get().getSurname());
        avatarItem.setDescription(null);
        avatarItem.setAvatar(new Avatar(authenticatedUser.get().get().getName() + " " + authenticatedUser.get().get().getSurname()));
    }
}
