package br.com.casa_moreno.casa_moreno_backend.email.controller;

import br.com.casa_moreno.casa_moreno_backend.email.dto.RegistrationEmailRequest;
import br.com.casa_moreno.casa_moreno_backend.email.service.EmailService;
import br.com.casa_moreno.casa_moreno_backend.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("EmailController Tests")
@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(emailController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    @DisplayName("Should send registration confirmation email successfully")
    void shouldSendRegistrationConfirmationEmailSuccessfully() throws Exception {
        RegistrationEmailRequest registrationEmailRequest = new RegistrationEmailRequest("email@email.com", "User Name");

        doNothing().when(emailService).sendRegistrationConfirmationEmail(registrationEmailRequest.to(), registrationEmailRequest.name());

        mockMvc.perform(
                        post("/email/send/registration-confirmation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registrationEmailRequest)))
                .andExpect(status().isAccepted());

        verify(emailService, times(1)).sendRegistrationConfirmationEmail(registrationEmailRequest.to(), registrationEmailRequest.name());
    }

    @Test
    @DisplayName("Should return Bad Request for invalid request body")
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        RegistrationEmailRequest invalidRequest = new RegistrationEmailRequest(null, "User Name");

        mockMvc.perform(
                        post("/email/send/registration-confirmation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendRegistrationConfirmationEmail(any(), any());
    }
}