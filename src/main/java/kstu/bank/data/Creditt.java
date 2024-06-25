package kstu.bank.data;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Creditt extends AbstractEntity {
    private String name;
    private double percent;
    private int period;
    private double minSum;
    private double maxSum;
    private double sum;
}
