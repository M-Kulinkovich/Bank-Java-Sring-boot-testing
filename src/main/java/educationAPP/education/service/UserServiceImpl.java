package educationAPP.education.service;

import educationAPP.education.config.JwtTokenProvider;
import educationAPP.education.dto.*;
import educationAPP.education.model.Role;
import educationAPP.education.model.User;
import educationAPP.education.repository.UserRepository;
import educationAPP.education.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TransactionsService transactionsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return buildBankResponse(AccountUtils.ACCOUNT_EXISTS_CODE, AccountUtils.ACCOUNT_EXISTS_MESSAGE, null);
        }

        User newUser = buildUser(userRequest);
        User savedUser = userRepository.save(newUser);

         EmailDetails emailDetails = buildEmailDetails(savedUser.getEmail(), "ACCOUNT CREATION",
                 "Your account has been created!\n Details: \n" + savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getEmail() + "\n");
         emailService.sendEmailAlert(emailDetails);

        AccountInfo accountInfo = buildAccountInfo(savedUser);
        return buildBankResponse(AccountUtils.ACCOUNT_CREATION_SUCCESS, AccountUtils.ACCOUNT_CREATION_MESSAGE, accountInfo);
    }

    @Override
    public BankResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

         EmailDetails loginAlert = buildEmailDetails(loginDto.getEmail(), "You're logged in",
                 "You logged into your account. If you did not initiate this request, please contact your bank");
         emailService.sendEmailAlert(loginAlert);

        return buildBankResponse("Login success", jwtTokenProvider.generateToken(authentication), null);
    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        User foundUser = findUserByAccountNumber(request.getAccountNumber());
        if (foundUser == null) {
            return buildBankResponse(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, null);
        }

        AccountInfo accountInfo = buildAccountInfo(foundUser);
        return buildBankResponse(AccountUtils.ACCOUNT_FOUND_CODE, AccountUtils.ACCOUNT_FOUND_MESSAGE, accountInfo);
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        User foundUser = findUserByAccountNumber(request.getAccountNumber());
        if (foundUser == null) {
            return AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE;
        }
        return foundUser.getFirstName() + " " + foundUser.getLastName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {
        User userToCredit = findUserByAccountNumber(request.getAccountNumber());
        if (userToCredit == null) {
            return buildBankResponse(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, null);
        }

        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userToCredit);

        saveTransaction(userToCredit.getAccountNumber(), "CREDIT", request.getAmount());

        AccountInfo accountInfo = buildAccountInfo(userToCredit);
        return buildBankResponse(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE, AccountUtils.ACCOUNT_CREDITED_MESSAGE, accountInfo);
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {
        User userToDebit = findUserByAccountNumber(request.getAccountNumber());
        if (userToDebit == null) {
            return buildBankResponse(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, null);
        }

        BigInteger availableBalance = userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = request.getAmount().toBigInteger();
        if (availableBalance.intValue() < debitAmount.intValue()) {
            return buildBankResponse(AccountUtils.INSUFFICIENT_BALANCE_CODE, AccountUtils.INSUFFICIENT_BALANCE_MESSAGE, null);
        }

        userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(userToDebit);

        saveTransaction(userToDebit.getAccountNumber(), "DEBIT", request.getAmount());

        AccountInfo accountInfo = buildAccountInfo(userToDebit);
        return buildBankResponse(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE, AccountUtils.ACCOUNT_DEBITED_MESSAGE, accountInfo);
    }

    @Override
    public BankResponse transfer(TransferRequest request) {
        User sourceAccount = findUserByAccountNumber(request.getSourceAccountNumber());
        User destinationAccount = findUserByAccountNumber(request.getDestinationAccountNumber());

        if (sourceAccount == null || destinationAccount == null) {
            return buildBankResponse(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, null);
        }

        if (request.getAmount().compareTo(sourceAccount.getAccountBalance()) > 0) {
            return buildBankResponse(AccountUtils.INSUFFICIENT_BALANCE_CODE, AccountUtils.INSUFFICIENT_BALANCE_MESSAGE, null);
        }

        sourceAccount.setAccountBalance(sourceAccount.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(sourceAccount);

         EmailDetails debitAlert = buildEmailDetails(sourceAccount.getEmail(), "DEBIT ALERT",
                 "The sum of " + request.getAmount() + " has been deducted from your account\n Your current balance is:" + sourceAccount.getAccountBalance());
         emailService.sendEmailAlert(debitAlert);

        destinationAccount.setAccountBalance(destinationAccount.getAccountBalance().add(request.getAmount()));
        userRepository.save(destinationAccount);

         EmailDetails creditAlert = buildEmailDetails(destinationAccount.getEmail(), "CREDIT ALERT",
                 "The sum of " + request.getAmount() + " has been sent to your account from" + sourceAccount.getFirstName() + sourceAccount.getLastName()
                         + "Your new balance is:" + sourceAccount.getAccountBalance());
         emailService.sendEmailAlert(creditAlert);

        saveTransaction(destinationAccount.getAccountNumber(), "CREDIT", request.getAmount());

        return buildBankResponse(AccountUtils.TRANSFER_SUCCESSFUL_CODE, AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE, null);
    }

    private User buildUser(UserRequest userRequest) {
        return User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(Role.valueOf("ROLE_ADMIN"))
                .accountBalance(BigDecimal.ZERO)
                .phoneNumber(userRequest.getPhoneNumber())
                .status("ACTIVE")
                .build();
    }

    private AccountInfo buildAccountInfo(User user) {
        return AccountInfo.builder()
                .accountBalance(user.getAccountBalance())
                .accountNumber(user.getAccountNumber())
                .accountName(user.getFirstName() + " " + user.getLastName())
                .build();
    }

    private BankResponse buildBankResponse(String responseCode, String responseMessage, AccountInfo accountInfo) {
        return BankResponse.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .accountInfo(accountInfo)
                .build();
    }

    private EmailDetails buildEmailDetails(String recipient, String subject, String messageBody) {
        return EmailDetails.builder()
                .recipient(recipient)
                .subject(subject)
                .messageBody(messageBody)
                .build();
    }

    private User findUserByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber);
    }

    private void saveTransaction(String accountNumber, String transactionType, BigDecimal amount) {
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(accountNumber)
                .transactionType(transactionType)
                .amount(amount)
                .build();
        transactionsService.saveTransaction(transactionDto);
    }
}
