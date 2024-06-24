package kstu.bank.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.persistence.EntityNotFoundException;
import kstu.bank.data.Role;
import kstu.bank.data.User;
import kstu.bank.data.UserRepository;
import kstu.bank.security.AuthenticatedUser;
import kstu.bank.services.UserService;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.HashSet;

@AnonymousAllowed
@PageTitle("Регистрация")
@Route(value = "register")
public class RegistrationView extends VerticalLayout {

    private final UserService userService;
    private final UserRepository userRepository;

    public RegistrationView(UserService userService, UserRepository userRepository) {
        this.userService = userService;

        // Заголовок формы
        H1 header = new H1("Регистрация");

        // Создаем форму регистрации
        FormLayout formLayout = new FormLayout();

        TextField username = new TextField("Логин");
        username.setRequired(true);
        TextField name = new TextField("Имя");
        TextField surname = new TextField("Фамилия");
        TextField phone = new TextField("Телефон");
        PasswordField password = new PasswordField("Пароль");
        PasswordField confirmPassword = new PasswordField("Подтвердите пароль");

        Button registerButton = new Button("Зарегистрироваться");

        // Обработка нажатия кнопки регистрации
        registerButton.addClickListener(event -> {
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Пароли не совпадают", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (username.getValue() == null || username.getValue().isEmpty()) {
                Notification.show("Введите логин", 3000, Notification.Position.MIDDLE);
                return;
            }
            try {
                userRepository.findByUsername(username.getValue());
                Notification.show("Пользователь с таким логином уже существует", 3000, Notification.Position.MIDDLE);
                return;

            } catch (EmptyResultDataAccessException | EntityNotFoundException e) {
                //skip
            }

            User user = new User();
            user.setUsername(username.getValue());
            user.setName(name.getValue());
            user.setSurname(surname.getValue());
            user.setPhoneNumber(phone.getValue());
            HashSet<Role> roles = new HashSet<>();
            roles.add(Role.USER);
            user.setRoles(roles);
            userService.save(user, password.getValue());
            Notification.show("Регистрация успешна", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate(LoginView.class);
        });

        // Добавляем компоненты в форму
        formLayout.addFormItem(username, "");
        formLayout.addFormItem(name, "");
        formLayout.addFormItem(surname, "");
        formLayout.addFormItem(phone, "");
        formLayout.addFormItem(password, "");
        formLayout.addFormItem(confirmPassword, "");

        // Настраиваем внешний вид кнопки
        registerButton.getElement().getStyle().set("margin-top", "20px");

        // Настраиваем внешний вид формы
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        add(header, formLayout, registerButton);
        getElement().getStyle().set("max-width", "500px");
        getElement().getStyle().set("margin", "0 auto");
        getElement().getStyle().set("padding", "20px");
        getElement().getStyle().set("background-color", "#f7f9fc");
        getElement().getStyle().set("border-radius", "8px");
        getElement().getStyle().set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)");
        this.userRepository = userRepository;
    }
}