package kstu.bank.views.credits;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Month;

@Getter
@Setter
public class Payment {
    @Getter
    private LocalDate paymentDate;
    @Getter
    private double paymentAmount;
    @Getter
    private double remainingDebt;

    // getters and setters
}
