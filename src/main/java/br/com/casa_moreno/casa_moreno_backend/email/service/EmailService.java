package br.com.casa_moreno.casa_moreno_backend.email.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateService templateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateService templateService) {
        this.mailSender = mailSender;
        this.templateService = templateService;
    }

    public void sendRegistrationConfirmationEmail(String to, String name) {
        final String subject = "Bem-vindo à Casa Moreno!";

        Map<String, Object> templateVariables = Map.of(
                "name", name,
                "subject", subject
        );

        String emailBody = templateService.processTemplate("registration-confirmation.html", templateVariables);

        sendHtmlEmail(to, subject, emailBody);
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Casa Moreno");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail para " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
