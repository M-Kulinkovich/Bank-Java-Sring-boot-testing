package educationAPP.education.controller;

import com.itextpdf.text.DocumentException;
import educationAPP.education.model.Transaction;
import educationAPP.education.service.BankStatement;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping ("/bankStatement")
@RequiredArgsConstructor
public class TransactionController {

    private BankStatement bankStatement;

    @GetMapping
    public List<Transaction> generateBankStatement(@RequestParam String accountNumber,
                                                   @RequestParam String startDate,
                                                   @RequestParam String endDate) throws DocumentException, FileNotFoundException, MessagingException {
        return bankStatement.generateStatement(accountNumber, startDate, endDate);
    }

}
