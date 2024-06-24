package kstu.bank.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByFrom_Username(String username);
    List<Transaction> findAllByTo_Username(String username);
}
