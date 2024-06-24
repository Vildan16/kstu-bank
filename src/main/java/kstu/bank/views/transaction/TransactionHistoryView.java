package kstu.bank.views.transaction;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;
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

@PageTitle("История операций")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class TransactionHistoryView extends Div {
    private AuthenticatedUser authenticatedUser;
    private CrmService crmService;

    private GridPro<Transaction> grid;
    private GridListDataView<Transaction> gridListDataView;

    private Grid.Column<Transaction> fromColumn;
    private Grid.Column<Transaction> toColumn;
    private Grid.Column<Transaction> sumColumn;

    private Grid.Column<Transaction> statusColumn;
    private Grid.Column<Transaction> dateTimeColumn;

    public TransactionHistoryView(AuthenticatedUser authenticatedUser, CrmService crmService) {
        this.authenticatedUser = authenticatedUser;
        this.crmService = crmService;
        addClassName("transaction-history-view");
        setSizeFull();
        createGrid();
        add(grid);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
    }

    private void createGridComponent() {
        grid = new GridPro<>();
        grid.setSelectionMode(SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("100%");

        List<Transaction> transactions = getTransactions();
        gridListDataView = grid.setItems(transactions);
    }

    private void addColumnsToGrid() {
        createFromColumn();
        createToColumn();
        createAmountColumn();
        createStatusColumn();
        createDateColumn();
    }

    private void createFromColumn() {
        fromColumn = grid.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(Alignment.CENTER);
            Avatar img = transaction.getFrom().getAvatar();
            Span span = new Span();
            span.setClassName("name");
            span.setText(transaction.getFrom().getName() + " " + transaction.getFrom().getSurname());
            hl.add(img, span);
            return hl;
        })).setComparator(transaction -> transaction.getFrom().getName()).setHeader("От");
    }

    private void createToColumn() {
        toColumn = grid.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(Alignment.CENTER);
            Avatar img = transaction.getTo().getAvatar();
            Span span = new Span();
            span.setClassName("name");
            span.setText(transaction.getTo().getName() + " " + transaction.getTo().getSurname());
            hl.add(img, span);
            return hl;
        })).setComparator(transaction -> transaction.getTo().getName()).setHeader("Кому");
    }

    private void createAmountColumn() {
        sumColumn = grid
                .addColumn(
                        new NumberRenderer<>(Transaction::getSum, Locale.of("ru")))
                .setComparator(Transaction::getSum).setHeader("Сумма");
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
        dateTimeColumn = grid
                .addColumn(new LocalDateTimeRenderer<>(Transaction::getDateTime,
                        () -> DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm")))
                .setComparator(Transaction::getDateTime).setHeader("Дата и время").setWidth("360px").setFlexGrow(0);
    }

    private List<Transaction> getTransactions() {
        if (authenticatedUser.get().get().getRoles().contains(Role.ADMIN)) {
            return crmService.getAllTransactions();
        } else {
            return crmService.getAllTransactionsByUsername(authenticatedUser.get().get().getUsername());
        }
    }

    private Client createClient(int id, String img, String client, double amount, String status, String date) {
        Client c = new Client();
        c.setId(id);
        c.setImg(img);
        c.setClient(client);
        c.setAmount(amount);
        c.setStatus(status);
        c.setDate(date);

        return c;
    }
}
