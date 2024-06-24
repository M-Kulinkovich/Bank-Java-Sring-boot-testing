package educationAPP.education;

import educationAPP.education.dto.TransactionDto;
import educationAPP.education.model.Transaction;
import educationAPP.education.repository.TransactionRepository;
import educationAPP.education.service.impl.TransactionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class TransactionsServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionsServiceImpl transactionsService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTransaction() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionType("CREDIT");
        transactionDto.setAccountNumber("1234567890");
        transactionDto.setAmount(new BigDecimal(100));

        transactionsService.saveTransaction(transactionDto);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        assertEquals("CREDIT", savedTransaction.getTransactionType());
        assertEquals("1234567890", savedTransaction.getAccountNumber());
        assertEquals(new BigDecimal(100), savedTransaction.getAmount());
        assertEquals("SUCCESS", savedTransaction.getStatus());
    }
}
