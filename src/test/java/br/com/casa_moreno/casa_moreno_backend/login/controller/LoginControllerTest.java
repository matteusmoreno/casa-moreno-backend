package br.com.casa_moreno.casa_moreno_backend.login.controller;

import br.com.casa_moreno.casa_moreno_backend.exception.GlobalExceptionHandler;
import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginRequest;
import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginResponse;
import br.com.casa_moreno.casa_moreno_backend.login.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("LoginController Tests")
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginService loginService;
    @InjectMocks
    private LoginController loginController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        loginRequest = new LoginRequest("username", "password");
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        LoginResponse loginResponse = new LoginResponse("tokenJwt", LocalDateTime.now().plusDays(1));

        when(loginService.loginAndGenerateToken(loginRequest)).thenReturn(loginResponse);

        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.token").value("tokenJwt"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.expiresAt").exists());

        verify(loginService, times(1)).loginAndGenerateToken(loginRequest);
    }

    @Test
    @DisplayName("Should return Bad Request for invalid login request")
    void shouldReturnBadRequestForInvalidLoginRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest(" ", "password");

        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(loginService, never()).loginAndGenerateToken(any());
    }

    @Test
    @DisplayName("Should return Unauthorized for bad credentials")
    void shouldReturnUnauthorizedForBadCredentials() throws Exception {
        when(loginService.loginAndGenerateToken(loginRequest))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(loginService, times(1)).loginAndGenerateToken(loginRequest);
    }
}