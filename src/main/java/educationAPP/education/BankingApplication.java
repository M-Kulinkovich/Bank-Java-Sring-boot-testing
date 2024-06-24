package educationAPP.education;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Java Spring Boot Bank App",
                description = "Backend REST API",
                version = "v1.0",
                contact = @Contact(
                        url = "https://github.com/M-Kulinkovich"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "The Java Bank App documentation"
        )
)
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

}
