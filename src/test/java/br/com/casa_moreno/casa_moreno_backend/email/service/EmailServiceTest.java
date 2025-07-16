package br.com.casa_moreno.casa_moreno_backend.email.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("EmailService Tests")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateService templateService;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@casamoreno.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Should send registration confirmation email with correct data")
    void shouldSendRegistrationConfirmationEmail() {
        String to = "test@example.com";
        String name = "Matteus";
        String expectedTemplate = "registration-confirmation.html";

        when(templateService.processTemplate(eq(expectedTemplate), any(Map.class))).thenReturn("<html>...</html>");

        emailService.sendRegistrationConfirmationEmail(to, name);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).processTemplate(eq(expectedTemplate), variablesCaptor.capture());

        assertEquals(name, variablesCaptor.getValue().get("name"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send OAuth registration welcome email with correct data")
    void shouldSendOAuthRegistrationWelcomeEmail() {
        String to = "oauth.user@example.com";
        String name = "OAuth User";
        String temporaryPassword = "temp-password-123";
        String expectedTemplate = "oauth-registration-welcome.html";

        when(templateService.processTemplate(eq(expectedTemplate), any(Map.class))).thenReturn("<html>...</html>");

        emailService.sendOAuthRegistrationWelcomeEmail(to, name, temporaryPassword);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).processTemplate(eq(expectedTemplate), variablesCaptor.capture());

        Map<String, Object> capturedVariables = variablesCaptor.getValue();
        assertEquals(name, capturedVariables.get("name"));
        assertEquals(temporaryPassword, capturedVariables.get("temporaryPassword"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send password reset link email with correct reset link")
    void shouldSendPasswordResetLinkEmail() {
        String to = "user@example.com";
        String name = "Jane Doe";
        String token = "12345-abcde-67890";
        String expectedTemplate = "password-reset-link.html";
        String expectedResetLink = "https://www.casa-moreno.com/auth/reset-password?token=" + token;

        when(templateService.processTemplate(eq(expectedTemplate), any(Map.class))).thenReturn("<html>...</html>");

        emailService.sendPasswordResetLinkEmail(to, name, token);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).processTemplate(eq(expectedTemplate), variablesCaptor.capture());

        assertEquals(expectedResetLink, variablesCaptor.getValue().get("resetLink"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send password change confirmation email")
    void shouldSendPasswordChangeConfirmationEmail() {
        String to = "user.changed@example.com";
        String name = "Changed User";
        String expectedTemplate = "password-change-confirmation.html";

        when(templateService.processTemplate(eq(expectedTemplate), any(Map.class))).thenReturn("<html>...</html>");

        emailService.sendPasswordChangeConfirmationEmail(to, name);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).processTemplate(eq(expectedTemplate), variablesCaptor.capture());

        assertEquals(name, variablesCaptor.getValue().get("name"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should handle exceptions during email sending gracefully")
    void shouldHandleExceptionsDuringEmailSending() {
        String to = "fail@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new RuntimeException("Failed to connect")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendHtmlEmail(to, subject, body));

        verify(mailSender).send(any(MimeMessage.class));
    }
}