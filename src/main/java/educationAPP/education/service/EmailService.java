package educationAPP.education.service;

import educationAPP.education.dto.EmailDetails;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
    void sendEmailWithAttachments(EmailDetails emailDetails) throws MessagingException;
}
