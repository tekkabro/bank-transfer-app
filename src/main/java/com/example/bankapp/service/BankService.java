package com.example.bankapp.service;

import com.example.bankapp.exception.*;
import com.example.bankapp.model.*;
import com.example.bankapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    @Transactional(rollbackFor = Exception.class)
   
    public void transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
        // Step 1: Validation
        if (fromAccount.equals(toAccount)) {
            throw new BankOperationException("Source and destination accounts must be different");
        }
        customerRepository.getByAccountNumber(fromAccount)
            .orElseThrow(() -> new AccountNotFoundException("Sender account not found: " + fromAccount));
        customerRepository.getByAccountNumber(toAccount)
            .orElseThrow(() -> new AccountNotFoundException("Receiver account not found: " + toAccount));

        // Step 2: Debit sender
        int updatedRows = customerRepository.debit(fromAccount, amount);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + fromAccount);
        }

        // Step 3: Simulated failure checkbox experiment
        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure after the withdrawal!");
        }

        // Step 4: Credit receiver
        customerRepository.credit(toAccount, amount);

        // Step 5: Save success transaction record
        BankTransaction tx = new BankTransaction();
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setDescription("Transfer completed");
        tx.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(tx);
    }

    @Transactional
    public void createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new BankOperationException("Account number already exists!");
        }
        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new BankOperationException("Email already exists!");
        }
        customerRepository.save(customer);
    }
}