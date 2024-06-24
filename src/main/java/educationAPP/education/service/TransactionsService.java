package educationAPP.education.service;

import educationAPP.education.dto.TransactionDto;
import educationAPP.education.model.Transaction;

public interface TransactionsService {
    void saveTransaction(TransactionDto transactionDto);
}
