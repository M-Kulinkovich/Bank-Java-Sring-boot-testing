package educationAPP.education.service.impl;

import educationAPP.education.dto.TransactionDto;
import educationAPP.education.model.Transaction;
import educationAPP.education.repository.TransactionRepository;
import educationAPP.education.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionRepository transactionRepository;

    @Override
    @Cacheable (value = "transactions", unless = "#result == null", key = "#accountNumber")
    public void saveTransaction(TransactionDto transactionDto) {
        Transaction transaction = Transaction.builder()
                .transactionType(transactionDto.getTransactionType())
                .accountNumber(transactionDto.getAccountNumber())
                .amount(transactionDto.getAmount())
                .status("SUCCESS")
                .build();
        transactionRepository.save(transaction);
        System.out.println("Transaction saved successfully");
    }
}

