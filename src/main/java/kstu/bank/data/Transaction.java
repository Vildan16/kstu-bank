package kstu.bank.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Transaction extends AbstractEntity {
    @ManyToOne
    private User from;
    @ManyToOne
    private User to;
    @Column
    private Long sum;
    @Column
    private String status;
    @Column
    private LocalDateTime dateTime;
}
