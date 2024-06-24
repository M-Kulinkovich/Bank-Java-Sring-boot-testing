package educationAPP.education;

import com.itextpdf.text.DocumentException;
import educationAPP.education.controller.TransactionController;
import educationAPP.education.model.Transaction;
import educationAPP.education.service.BankStatement;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransactionControllerTest {

    @Mock
    private BankStatement bankStatement;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    public void testGenerateBankStatement_Success() throws Exception {
        List<Transaction> mockTransactions = new ArrayList<>();
        mockTransactions.add(new Transaction("1", "Deposit", new BigDecimal("100.0"), "1234567890", "Success", LocalDateTime.now(), LocalDateTime.now()));
        when(bankStatement.generateStatement(anyString(), anyString(), anyString())).thenReturn(mockTransactions);

        mockMvc.perform(get("/bankStatement")
                        .param("accountNumber", "1234567890")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].transactionId").value("1"))
                .andExpect(jsonPath("$[0].transactionType").value("Deposit"))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].status").value("Success"));
    }

    @Test
    public void testGenerateBankStatement_NoTransactions() throws Exception {
        List<Transaction> mockTransactions = new ArrayList<>();
        when(bankStatement.generateStatement(anyString(), anyString(), anyString())).thenReturn(mockTransactions);

        mockMvc.perform(get("/bankStatement")
                        .param("accountNumber", "1234567890")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGenerateBankStatement_MissingParameters() throws Exception {
        mockMvc.perform(get("/bankStatement")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
