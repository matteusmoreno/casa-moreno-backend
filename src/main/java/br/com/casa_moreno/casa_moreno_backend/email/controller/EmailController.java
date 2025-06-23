package br.com.casa_moreno.casa_moreno_backend.email.controller;

import br.com.casa_moreno.casa_moreno_backend.email.service.EmailService;
import br.com.casa_moreno.casa_moreno_backend.email.dto.RegistrationEmailRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send/registration-confirmation")
    public ResponseEntity<String> sendRegistrationEmail(@RequestBody @Valid RegistrationEmailRequest request) {
        emailService.sendRegistrationConfirmationEmail(request.to(), request.name());

        return ResponseEntity.accepted().body("Confirmation email sent successfully.");
    }
}
