package educationAPP.education;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import educationAPP.education.config.JwtTokenProvider;
import educationAPP.education.dto.*;
import educationAPP.education.model.Role;
import educationAPP.education.model.User;
import educationAPP.education.repository.UserRepository;
import educationAPP.education.service.*;
import educationAPP.education.utils.AccountUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;


@ExtendWith (MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .gender("Male")
                .address("123 Main St")
                .stateOfOrigin("SomeState")
                .accountNumber("1234567890")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.ROLE_ADMIN)
                .accountBalance(BigDecimal.ZERO)
                .phoneNumber("1234567890")
                .status("ACTIVE")
                .build();

        userRequest = new UserRequest();
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setGender("Male");
        userRequest.setAddress("123 Main St");
        userRequest.setStateOfOrigin("SomeState");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setPassword("password");
        userRequest.setPhoneNumber("1234567890");
    }

    @Test
    void testCreateAccount_UserAlreadyExists() {
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        BankResponse response = userService.createAccount(userRequest);

        assertEquals(AccountUtils.ACCOUNT_EXISTS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_EXISTS_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testCreateAccount_Success() {
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        BankResponse response = userService.createAccount(userRequest);

        assertEquals(AccountUtils.ACCOUNT_CREATION_SUCCESS, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_CREATION_MESSAGE, response.getResponseMessage());
        assertNotNull(response.getAccountInfo());
        assertEquals(user.getAccountNumber(), response.getAccountInfo().getAccountNumber());
    }

    @Test
    void testLogin_Success() {
        LoginDto loginDto = new LoginDto("john.doe@example.com", "password");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwtToken");

        BankResponse response = userService.login(loginDto);

        assertEquals("Login success", response.getResponseCode());
        assertEquals("jwtToken", response.getResponseMessage());
    }

    @Test
    void testBalanceEnquiry_AccountNotExists() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(null);

        EnquiryRequest request = new EnquiryRequest("1234567890");
        BankResponse response = userService.balanceEnquiry(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testBalanceEnquiry_Success() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(user);

        EnquiryRequest request = new EnquiryRequest("1234567890");
        BankResponse response = userService.balanceEnquiry(request);

        assertEquals(AccountUtils.ACCOUNT_FOUND_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_FOUND_MESSAGE, response.getResponseMessage());
        assertNotNull(response.getAccountInfo());
        assertEquals(user.getAccountNumber(), response.getAccountInfo().getAccountNumber());
    }

    @Test
    void testCreditAccount_AccountNotExists() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(null);

        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal(100));
        BankResponse response = userService.creditAccount(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testCreditAccount_Success() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(user);

        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal(100));
        user.setAccountBalance(new BigDecimal(100));

        BankResponse response = userService.creditAccount(request);

        assertEquals(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_CREDITED_MESSAGE, response.getResponseMessage());
        assertNotNull(response.getAccountInfo());
        assertEquals(user.getAccountNumber(), response.getAccountInfo().getAccountNumber());
    }

    @Test
    void testDebitAccount_AccountNotExists() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(null);

        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal(100));
        BankResponse response = userService.debitAccount(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testDebitAccount_InsufficientBalance() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(user);

        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal(100));
        user.setAccountBalance(new BigDecimal(50));

        BankResponse response = userService.debitAccount(request);

        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_CODE, response.getResponseCode());
        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testDebitAccount_Success() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(user);

        CreditDebitRequest request = new CreditDebitRequest("1234567890", new BigDecimal(50));
        user.setAccountBalance(new BigDecimal(100));

        BankResponse response = userService.debitAccount(request);

        assertEquals(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_DEBITED_MESSAGE, response.getResponseMessage());
        assertNotNull(response.getAccountInfo());
        assertEquals(user.getAccountNumber(), response.getAccountInfo().getAccountNumber());
    }

    @Test
    void testTransfer_AccountNotExists() {
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(null);

        TransferRequest request = new TransferRequest("1234567890", "0987654321", new BigDecimal(100));
        BankResponse response = userService.transfer(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, response.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testTransfer_InsufficientBalance() {
        User sourceUser = user;
        sourceUser.setAccountBalance(new BigDecimal(50));

        when(userRepository.findByAccountNumber("1234567890")).thenReturn(sourceUser);
        when(userRepository.findByAccountNumber("0987654321")).thenReturn(user);

        TransferRequest request = new TransferRequest("1234567890", "0987654321", new BigDecimal(100));
        BankResponse response = userService.transfer(request);

        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_CODE, response.getResponseCode());
        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());
    }

    @Test
    void testTransfer_Success() {
        User sourceUser = new User();
        sourceUser.setAccountNumber("1234567890");
        sourceUser.setAccountBalance(new BigDecimal(150));

        User destinationUser = new User();
        destinationUser.setAccountNumber("0987654321");
        destinationUser.setAccountBalance(new BigDecimal(50));

        when(userRepository.findByAccountNumber("1234567890")).thenReturn(sourceUser);
        when(userRepository.findByAccountNumber("0987654321")).thenReturn(destinationUser);
        TransferRequest request = new TransferRequest("1234567890", "0987654321", new BigDecimal(100));
        BankResponse response = userService.transfer(request);

        assertEquals(AccountUtils.TRANSFER_SUCCESSFUL_CODE, response.getResponseCode());
        assertEquals(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE, response.getResponseMessage());
        assertNull(response.getAccountInfo());

        assertEquals(new BigDecimal(50), sourceUser.getAccountBalance());
        assertEquals(new BigDecimal(150), destinationUser.getAccountBalance());
    }
}
