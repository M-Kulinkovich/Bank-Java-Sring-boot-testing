package educationAPP.education;

import com.fasterxml.jackson.databind.ObjectMapper;
import educationAPP.education.controller.UserController;
import educationAPP.education.dto.*;
import educationAPP.education.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateAccount_Success() throws Exception {
        UserRequest userRequest = new UserRequest("John", "Doe", "City", "Male", "Address", "State", "john.doe@example.com", "password", "1234567890");
        BankResponse mockResponse = new BankResponse("201", "Account created successfully", null);
        when(userService.createAccount(any(UserRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode").value("201"))
                .andExpect(jsonPath("$.responseMessage").value("Account created successfully"));
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginDto loginDto = new LoginDto("john.doe@example.com", "password");
        BankResponse mockResponse = new BankResponse("200", "Login successful", null);
        when(userService.login(any(LoginDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.responseMessage").value("Login successful"));
    }

    @Test
    public void testBalanceEnquiry_Success() throws Exception {
        EnquiryRequest enquiryRequest = new EnquiryRequest("1234567890");
        BankResponse mockResponse = new BankResponse("200", "Balance enquiry successful", new AccountInfo("John Doe", new BigDecimal("1000.00"), "1234567890"));
        when(userService.balanceEnquiry(any(EnquiryRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/user/balanceEnquiry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enquiryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.responseMessage").value("Balance enquiry successful"))
                .andExpect(jsonPath("$.accountInfo.accountName").value("John Doe"))
                .andExpect(jsonPath("$.accountInfo.accountBalance").value(1000.00))
                .andExpect(jsonPath("$.accountInfo.accountNumber").value("1234567890"));
    }

    @Test
    public void testNameEnquiry_Success() throws Exception {
        EnquiryRequest enquiryRequest = new EnquiryRequest("1234567890");
        String mockResponse = "John Doe";
        when(userService.nameEnquiry(any(EnquiryRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/user/nameEnquiry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enquiryRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("John Doe"));
    }

    @Test
    public void testCreditAccount_Success() throws Exception {
        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal("500.00"));
        BankResponse mockResponse = new BankResponse("200", "Account credited successfully", null);
        when(userService.creditAccount(any(CreditDebitRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/user/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.responseMessage").value("Account credited successfully"));
    }

    @Test
    public void testDebitAccount_Success() throws Exception {
        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal("200.00"));
        BankResponse mockResponse = new BankResponse("200", "Account debited successfully", null);
        when(userService.debitAccount(any(CreditDebitRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/user/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.responseMessage").value("Account debited successfully"));
    }

    @Test
    public void testTransfer_Success() throws Exception {
        TransferRequest request = new TransferRequest("1234567890", "0987654321", new BigDecimal("300.00"));
        BankResponse mockResponse = new BankResponse("200", "Transfer successful", null);
        when(userService.transfer(any(TransferRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/user/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("200"))
                .andExpect(jsonPath("$.responseMessage").value("Transfer successful"));
    }
}
