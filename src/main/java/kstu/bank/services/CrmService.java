package kstu.bank.services;

import kstu.bank.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrmService {

    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CreditRepository creditRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCreditRepository userCreditRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll(Sort.by("dateTime"));
    }

    public List<Transaction> getAllTransactionsByUsername(String username) {
        List<Transaction> allByFromUsername = transactionRepository.findAllByFrom_Username(username);
        List<Transaction> allByToUsername = transactionRepository.findAllByTo_Username(username);
        Set<Transaction> result = new HashSet<>();
        result.addAll(allByFromUsername);
        result.addAll(allByToUsername);
        return result.stream().sorted(Comparator.comparing(Transaction::getDateTime)).collect(Collectors.toList());
    }

    public List<Account> getAllAccounts(User user) {
        if (user == null) {
            return accountRepository.findAll();
        }
        return accountRepository.findAllByUser(user);
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public void openAccount(User user) {
        if (user == null) {
            return;
        }
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateRandomDigitsString(20));
        account.setStatus("Открыт");
        account.setBalance(0L);
        account.setOpeningDate(LocalDate.now());
        accountRepository.save(account);
    }

    private static String generateRandomDigitsString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int digit = random.nextInt(1, 10);  // Generates a random number between 0 and 9
            sb.append(digit);
        }
        return sb.toString();
    }

    public Long getBalance(User user) {
        List<Account> allAccounts = getAllAccounts(user);
        return allAccounts.stream().map(Account::getBalance).reduce(0L, Long::sum);
    }

    public void saveTransaction(Transaction transaction, Account accountFrom, Account accountTo) {
        transactionRepository.save(transaction);
        accountRepository.save(accountFrom);
        accountRepository.save(accountTo);
    }

    public void saveTransaction(Transaction transaction, Account accountTo) {
        transactionRepository.save(transaction);
        accountRepository.save(accountTo);
    }

    public void closeAccount(Account account) {
        account.setStatus("Закрыт");
        accountRepository.save(account);
    }

    public List<Creditt> getAllCredits() {
        return creditRepository.findAll();
    }

    public void createCredit(Creditt credit) {
        creditRepository.save(credit);
    }

    public void deleteCredit(Long id) {
        creditRepository.deleteById(id);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void saveUserCreditt(UserCreditt creditt) {
        userCreditRepository.save(creditt);
    }
}
