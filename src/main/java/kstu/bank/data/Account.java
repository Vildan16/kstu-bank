package kstu.bank.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Account extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne
    private User user;

    @Column
    private Long balance;

    @Column
    private LocalDate openingDate;

    @Column
    private String status;
}
