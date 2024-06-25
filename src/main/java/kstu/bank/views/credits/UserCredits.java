package kstu.bank.views.credits;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import kstu.bank.data.*;
import kstu.bank.security.AuthenticatedUser;
import kstu.bank.services.CrmService;
import kstu.bank.views.MainLayout;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@PageTitle("Мои кредиты")
@Route(value = "user-credits", layout = MainLayout.class)
@RolesAllowed(value = {"ROLE_USER"})
public class UserCredits extends VerticalLayout {
    private final CrmService crmService;
    private final AuthenticatedUser authenticatedUser;
    private final Button showScheduleButton;
    private Grid<UserCreditt> grid;
    private Button createButton;
    private Dialog dialog;
    private FormLayout formLayout;
    private Binder<Creditt> binder;
    private Creditt currentCredit;

    public UserCredits(CrmService crmService, AuthenticatedUser authenticatedUser) {
        this.crmService = crmService;
        this.authenticatedUser = authenticatedUser;
        createGrid();

        createButton = new Button("Оформить", click -> {
            openCreditSelectionDialog();
        });
        showScheduleButton = new Button("Показать график платежей", click -> {
            if (grid.asSingleSelect().getValue() != null) {
                showPaymentSchedule(grid.asSingleSelect().getValue());
            } else {
                Notification.show("Выберите кредит").setPosition(Notification.Position.MIDDLE);
            }
        });
        showScheduleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(new HorizontalLayout(createButton, showScheduleButton));

        add(grid);
    }

    private void createGrid() {
        grid = new Grid<>(UserCreditt.class);
        grid.setColumns("name", "percent", "period", "sum", "futureSum");
        grid.getColumnByKey("name").setHeader("Название");
        grid.getColumnByKey("percent").setHeader("Процентная ставка (%)");
        grid.getColumnByKey("period").setHeader("Период (мес.)");
        grid.getColumnByKey("sum").setHeader("Сумма (руб.)");
        grid.getColumnByKey("futureSum").setHeader("Остаток долга (руб.)");
        grid.addColumn(a -> a.getAccount() == null ? null : a.getAccount().getAccountNumber()).setHeader("Привязанный счет");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        updateGrid();
    }

    private void updateGrid() {
        grid.setItems(authenticatedUser.get().get().getCredits());
    }

    private void openCreditSelectionDialog() {
        Dialog selectionDialog = new Dialog();
        selectionDialog.setSizeFull();
        Grid<Creditt> selectionGrid = new Grid<>(Creditt.class);
        selectionGrid.setItems(crmService.getAllCredits()); // предполагается, что этот метод возвращает все доступные кредиты
        selectionGrid.setColumns("name", "percent", "period", "minSum", "maxSum");
        selectionGrid.getColumnByKey("name").setHeader("Название");
        selectionGrid.getColumnByKey("percent").setHeader("Процентная ставка (%)");
        selectionGrid.getColumnByKey("period").setHeader("Период (мес.)");
        selectionGrid.getColumnByKey("minSum").setHeader("Минимальная сумма (руб.)");
        selectionGrid.getColumnByKey("maxSum").setHeader("Максимальная сумма (руб.)");
        selectionGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        selectionGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                currentCredit = event.getValue();
                selectionDialog.close();
                openDialog();
            }
        });
        selectionDialog.add(new H4("Доступные кредиты"), selectionGrid, new Button("Отмена", click -> selectionDialog.close()));
        selectionDialog.open();
    }

    private void openDialog() {
        dialog = new Dialog();
        formLayout = new FormLayout();
        binder = new Binder<>(Creditt.class);

        TextField nameField = new TextField("Название");
        NumberField percent = new NumberField("Процентная ставка (%)");
        IntegerField period = new IntegerField("Срок (мес.)");
        TextField sum = new TextField("Cумма (руб.)");
        TextField futureSum = new TextField("Общая сумма займа с учетом % (руб.)");
        ComboBox<Account> accountComboBox = new ComboBox<>("Счет начисления");
        accountComboBox.setItems(crmService.getAllAccounts(authenticatedUser.get().get()));
        accountComboBox.setItemLabelGenerator(Account::getAccountNumber);
        binder.forField(nameField)
                .withValidator(new StringLengthValidator(
                        "Введите название", 1, null))
                .bind(Creditt::getName, Creditt::setName);

        binder.forField(percent)
                .withValidator(new DoubleRangeValidator(
                        "Введите значение", 0.01, null))
                .bind(Creditt::getPercent, Creditt::setPercent);

        formLayout.add(new H4("Условия кредита"));
        formLayout.add(nameField, percent, period, sum, futureSum, accountComboBox);
        nameField.setReadOnly(true);
        nameField.setValue(currentCredit.getName());
        percent.setReadOnly(true);
        percent.setValue(currentCredit.getPercent());
        period.setReadOnly(true);
        period.setValue(currentCredit.getPeriod());
        futureSum.setReadOnly(true);
        sum.addValueChangeListener(event -> {
            futureSum.setValue(calculateTotalCreditSum(Double.parseDouble(sum.getValue()), currentCredit.getPercent(), currentCredit.getPeriod()));
        });

        formLayout.setSizeFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setMaxWidth("500px");

        Button saveButton = new Button("Оформить", click -> {
            if (Long.parseLong(sum.getValue()) < currentCredit.getMinSum() || Double.parseDouble(sum.getValue()) > currentCredit.getMaxSum()) {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Сумма не соответствует условиям кредита");
                confirmDialog.setText("Сумма должна быть в диапазоне от " + currentCredit.getMinSum() + " до " + currentCredit.getMaxSum() + " руб.");
                confirmDialog.setConfirmText("ОК");
                confirmDialog.setCancelable(false);
                confirmDialog.open();
                return;
            }
            saveCredit(period.getValue(), Double.valueOf(sum.getValue()), Double.valueOf(futureSum.getValue()), accountComboBox.getValue());
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        formLayout.add(new HorizontalLayout(saveButton, new Button("Отмена", click -> dialog.close())));

        dialog.add(formLayout);
        dialog.open();
    }

    private void saveCredit(Integer period, Double sum, Double futureSum, Account account) {
        User user = authenticatedUser.get().get();
        UserCreditt creditt = new UserCreditt();
        creditt.setAccount(account);
        creditt.setPercent(currentCredit.getPercent());
        creditt.setPeriod(period);
        creditt.setSum(sum);
        creditt.setFutureSum(futureSum);
        creditt.setName(currentCredit.getName());
        creditt.setPercent(currentCredit.getPercent());
        user.getCredits().add(creditt);
        account.setBalance(account.getBalance() + sum.longValue());
        Transaction transaction = new Transaction();
        transaction.setDateTime(java.time.LocalDateTime.now());
        User user1 = new User();
        user1.setName("Банк");
        user1.setSurname("КНИТУ");
        crmService.saveUser(user1);
        transaction.setFrom(user1);
        transaction.setStatus("Выдача кредита");
        transaction.setSum(sum.longValue());
        transaction.setTo(authenticatedUser.get().get());
        crmService.saveTransaction(transaction, account);
        crmService.saveUserCreditt(creditt);
        crmService.saveUser(user);
        dialog.close();
        Notification.show("Кредит оформлен").setPosition(Notification.Position.MIDDLE);
        updateGrid();
    }

    public String calculateTotalCreditSum(double loanSum, double interestRate, int loanPeriod) {
        double total = loanSum * Math.pow(1 + interestRate / 100, loanPeriod);
        return String.format("%.2f", total).replace(",", ".");
    }

    private void showPaymentSchedule(UserCreditt credit) {
        // Создаем новый диалог для отображения графика платежей
        Dialog scheduleDialog = new Dialog();
        scheduleDialog.setSizeFull();

        // Создаем новый грид для отображения графика платежей
        Grid<Payment> scheduleGrid = new Grid<>(Payment.class);
        DecimalFormat df = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        scheduleGrid.removeAllColumns();

        scheduleGrid.addColumn(p -> DateTimeFormatter.ofPattern("dd.MM.yyyy").format(p.getPaymentDate())).setHeader("Дата платежа");

        scheduleGrid.addColumn(payment -> {
            double amount = payment.getPaymentAmount();
            // Форматируем сумму с двумя знаками после запятой
            return amount <= 0d ? "0" : df.format(amount);
        }).setHeader("Сумма платежа");

        scheduleGrid.addColumn(payment -> {
            double remainingDebt = payment.getRemainingDebt();
            // Форматируем остаток долга с двумя знаками после запятой
            return remainingDebt <= 1d ? "0" : df.format(remainingDebt);
        }).setHeader("Остаток долга");

        scheduleGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        // Получаем график платежей для данного кредита
        List<Payment> paymentSchedule = calculatePaymentSchedule(credit);
        scheduleGrid.setItems(paymentSchedule);

        scheduleDialog.add(new H4("График платежей"), scheduleGrid, new Button("Закрыть", click -> scheduleDialog.close()));
        scheduleDialog.open();
    }

    private List<Payment> calculatePaymentSchedule(UserCreditt credit) {
        double monthlyInterestRate = credit.getPercent() / 12 / 100;
        double annuityPayment = credit.getSum() * (monthlyInterestRate / (1 - Math.pow(1 + monthlyInterestRate, -credit.getPeriod())));
        double remainingDebt = credit.getSum();

        List<Payment> paymentSchedule = new ArrayList<>();
        for (int month = 1; month <= credit.getPeriod(); month++) {
            double monthlyInterest = remainingDebt * monthlyInterestRate;
            double principalPayment = annuityPayment - monthlyInterest;
            remainingDebt -= principalPayment;

            Payment paymentInfo = new Payment();
            paymentInfo.setPaymentDate(LocalDate.now().plusMonths(month));
            paymentInfo.setPaymentAmount(annuityPayment);
            paymentInfo.setRemainingDebt(remainingDebt);

            paymentSchedule.add(paymentInfo);
        }
        return paymentSchedule;
    }

}
