package kstu.bank.views.credits;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import kstu.bank.data.Creditt;
import kstu.bank.services.CrmService;
import kstu.bank.views.MainLayout;

@PageTitle("Доступные кредиты")
@Route(value = "available-credits", layout = MainLayout.class)
@RolesAllowed(value = {"ROLE_ADMIN"})
public class AvailableCredits extends VerticalLayout {
    private final CrmService crmService;
    private Grid<Creditt> grid;
    private Button createButton;
    private Button deleteButton;
    private Dialog dialog;
    private FormLayout formLayout;
    private Binder<Creditt> binder;
    private Creditt currentCredit;

    public AvailableCredits(CrmService crmService) {
        this.crmService = crmService;
        createGrid();

        createButton = new Button("Создать", click -> openDialog(new Creditt()));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton = new Button("Удалить", click -> deleteCredit());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(new HorizontalLayout(createButton, deleteButton));

        add(grid);

        dialog = new Dialog();
        formLayout = new FormLayout();
        binder = new Binder<>(Creditt.class);

        TextField nameField = new TextField("Название");
        NumberField percent = new NumberField("Процентная ставка (%)");
        IntegerField period = new IntegerField("Срок (мес.)");
        TextField minSum = new TextField("Минимальная сумма (руб.)");
        TextField maxSum = new TextField("Максимальная сумма (руб.)");
        binder.forField(nameField)
                .withValidator(new StringLengthValidator(
                        "Введите название", 1, null))
                .bind(Creditt::getName, Creditt::setName);

        binder.forField(percent)
                .withValidator(new DoubleRangeValidator(
                        "Введите значение", 0.01, null))
                .bind(Creditt::getPercent, Creditt::setPercent);

        binder.forField(period)
                .withValidator(new IntegerRangeValidator(
                        "Введите значение", 1, null))
                .bind(Creditt::getPeriod, Creditt::setPeriod);
        binder.forField(minSum)
                .withValidator(new StringLengthValidator(
                        "Введите значение", 1, null))
                .bind(creditt -> String.valueOf(creditt.getMinSum()), (creditt, string) -> creditt.setMinSum(Double.parseDouble(string)));
        binder.forField(maxSum)
                .withValidator(new StringLengthValidator(
                        "Введите значение", 1, null))
                .bind(creditt -> String.valueOf(creditt.getMaxSum()), (creditt, string) -> creditt.setMaxSum(Double.parseDouble(string)));
        formLayout.add(new H4("Добавление доступного кредита"));
        formLayout.add(nameField, percent, period, minSum, maxSum);
        formLayout.setSizeFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setMaxWidth("500px");

        Button saveButton = new Button("Сохранить", click -> saveCredit());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        formLayout.add(new HorizontalLayout(saveButton, new Button("Отмена", click -> dialog.close())));

        dialog.add(formLayout);
    }

    private void createGrid() {
        grid = new Grid<>(Creditt.class);
        grid.setColumns("name", "percent", "period", "minSum", "maxSum");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.getColumnByKey("name").setHeader("Название");
        grid.getColumnByKey("percent").setHeader("Процентная ставка (%)");
        grid.getColumnByKey("period").setHeader("Период (мес.)");
        grid.getColumnByKey("minSum").setHeader("Минимальная сумма (руб.)");
        grid.getColumnByKey("maxSum").setHeader("Максимальная сумма (руб.)");
        updateGrid();
    }

    private void updateGrid() {
        grid.setItems(crmService.getAllCredits());
    }

    private void openDialog(Creditt credit) {
        currentCredit = credit;
        binder.setBean(credit);
        dialog.open();
    }

    private void saveCredit() {
        if (binder.validate().isOk()) {
            crmService.createCredit(currentCredit);
            dialog.close();
            updateGrid();
        }
    }

    private void deleteCredit() {
        Creditt selectedCredit = grid.asSingleSelect().getValue();
        if (selectedCredit != null) {
            crmService.deleteCredit(selectedCredit.getId());
            updateGrid();
        }
    }
}
