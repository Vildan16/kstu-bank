package kstu.bank.views.transaction;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import kstu.bank.data.Account;
import kstu.bank.data.Role;
import kstu.bank.data.Transaction;
import kstu.bank.security.AuthenticatedUser;
import kstu.bank.services.CrmService;
import kstu.bank.views.MainLayout;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@PageTitle("Мои счета")
@Route(value = "accounts", layout = MainLayout.class)
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
public class AccountGrid extends Div {
    private AuthenticatedUser authenticatedUser;
    private CrmService crmService;

    private GridPro<Account> grid;
    private GridListDataView<Account> gridListDataView;

    private Grid.Column<Account> numberColumn;
    private Grid.Column<Account> balanceColumn;
    private Grid.Column<Account> statusColumn;
    private Grid.Column<Account> dateColumn;

    public AccountGrid(AuthenticatedUser authenticatedUser, CrmService crmService) {
        this.authenticatedUser = authenticatedUser;
        this.crmService = crmService;
        addClassName("transaction-history-view");
        setSizeFull();
        createGrid();
        HorizontalLayout horizontalLayout = buttonLayout();
        add(horizontalLayout, grid);
    }

    private HorizontalLayout buttonLayout() {
        Button createButton = new Button("Открыть счет");
        createButton.addClickListener(e -> openAccDialog());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button closeButton = new Button("Закрыть счет");
        closeButton.addClickListener(e -> closeDialog());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        return new HorizontalLayout(createButton, closeButton);
    }

    private void closeDialog() {
        if (grid.getSelectedItems().stream().findFirst().isEmpty()) {
            Notification.show("Выберите счет");
        }
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Закрытие счета");
        dialog.setText("Вы действительно хотите закрыть счет?");
        dialog.setConfirmText("Да");
        dialog.addConfirmListener(e -> {
            crmService.closeAccount(grid.getSelectedItems().stream().findFirst().get());
            Notification.show("Счет открыт!");
        });
        dialog.setCancelable(true);
        dialog.setCancelText("Отмена");
        dialog.addDetachListener(e -> {
            List<Account> accounts = getAccounts();
            gridListDataView = grid.setItems(accounts);
        });
        dialog.setOpened(true);
    }

    private void openAccDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Открытие счета");
        dialog.setText("Вы действительно хотите открыть новый счет?");
        dialog.setConfirmText("Да");
        dialog.addConfirmListener(e -> {
           crmService.openAccount(authenticatedUser.get().get());
            Notification.show("Счет открыт!");
        });
        dialog.setCancelable(true);
        dialog.setCancelText("Отмена");
        dialog.addDetachListener(e -> {
            List<Account> accounts = getAccounts();
            gridListDataView = grid.setItems(accounts);
        });
        dialog.setOpened(true);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
    }

    private void createGridComponent() {
        grid = new GridPro<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("100%");

        List<Account> accounts = getAccounts();
        gridListDataView = grid.setItems(accounts);
    }

    private void addColumnsToGrid() {
        createFromColumn();
        createAmountColumn();
        createStatusColumn();
        createDateColumn();
    }

    private void createFromColumn() {
        numberColumn = grid.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("name");
            span.setText(transaction.getAccountNumber());
            hl.add(span);
            return hl;
        })).setHeader("Номер счета");
    }

    private void createAmountColumn() {
        balanceColumn = grid
                .addColumn(
                        new NumberRenderer<>(Account::getBalance, Locale.of("ru")))
                .setComparator(Account::getBalance).setHeader("Баланс");
    }

    private void createStatusColumn() {
        statusColumn = grid.addColumn(new ComponentRenderer<>(transaction -> {
            Span span = new Span();
            span.setText(transaction.getStatus());
            span.getElement().setAttribute("theme", "badge " + transaction.getStatus().toLowerCase());
            return span;
        })).setHeader("Статус");
    }

    private void createDateColumn() {
        dateColumn = grid
                .addColumn(new LocalDateRenderer<>(Account::getOpeningDate,
                        () -> DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .setComparator(Account::getOpeningDate).setHeader("Дата открытия").setWidth("180px").setFlexGrow(0);
    }

    private List<Account> getAccounts() {
        if (authenticatedUser.get().get().getRoles().contains(Role.ADMIN)) {
            return crmService.getAllAccounts(null);
        } else {
            return crmService.getAllAccounts(authenticatedUser.get().get());
        }
    }
}