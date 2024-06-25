package kstu.bank.data;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserCreditt extends AbstractEntity {
    private String name;
    private double percent;
    private int period;
    private double sum;
    private double futureSum;
    @ManyToOne
    private Account account;
}
