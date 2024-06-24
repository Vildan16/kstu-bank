package kstu.bank.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.component.avatar.Avatar;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "application_user")
public class User extends AbstractEntity {

    private String username;
    private String name;
    @JsonIgnore
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Column
    private String surname;

    @Column
    private LocalDate birthDate;

    @Column
    private String phoneNumber;

    @Column
    @Email
    private String email;

    @Column
    private Long balance = 0L;

    @OneToMany
    private List<Account> accounts;

    public Avatar getAvatar() {
        return new Avatar(name + " " + surname);
    }
}
