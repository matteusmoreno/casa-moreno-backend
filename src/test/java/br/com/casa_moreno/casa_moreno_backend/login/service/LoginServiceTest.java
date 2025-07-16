package br.com.casa_moreno.casa_moreno_backend.login.service;

import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginRequest;
import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginResponse;
import br.com.casa_moreno.casa_moreno_backend.security.TokenService;
import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@DisplayName("LoginService Tests")
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    TokenService tokenService;
    @InjectMocks
    LoginService loginService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(UUID.randomUUID())
                .name("Name")
                .username("username")
                .password("encodedPassword")
                .email("user@user.com")
                .phone("22888888888")
                .profile(Profile.USER)
                .profilePictureUrl(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

    }

    @Test
    @DisplayName("Should login and generate token successfully")
    void shouldLoginAndGenerateTokenSuccessfully() {
        String rawPassword = "password123";
        LoginRequest loginRequest = new LoginRequest(user.getUsername(), rawPassword);

        when(userRepository.findByUsernameOrEmail(loginRequest.username())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("generatedToken");

        LoginResponse response = loginService.loginAndGenerateToken(loginRequest);

        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.username());
        verify(tokenService, times(1)).generateToken(user);
        verify(userRepository, times(1)).save(user);

        assertAll(
                () -> Assertions.assertNotNull(response),
                () -> Assertions.assertEquals("generatedToken", response.token()),
                () -> Assertions.assertTrue(response.expiresAt().isAfter(LocalDateTime.now()))
        );
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when username or email is incorrect")
    void shouldThrowBadCredentialsExceptionWhenUsernameOrEmailIsIncorrect() {
        String incorrectUsername = "incorrectLUsername";
        LoginRequest loginRequest = new LoginRequest(incorrectUsername, "password123");

        when(userRepository.findByUsernameOrEmail(incorrectUsername)).thenReturn(Optional.empty());

        Assertions.assertThrows(BadCredentialsException.class, () -> {
            loginService.loginAndGenerateToken(loginRequest);
        });

        verify(userRepository, times(1)).findByUsernameOrEmail(incorrectUsername);
        verify(tokenService, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password is incorrect")
    void shouldThrowBadCredentialsExceptionWhenPasswordIsIncorrect() {
        String incorrectPassword = "incorrectPassword";
        LoginRequest loginRequest = new LoginRequest(user.getUsername(), incorrectPassword);

        when(userRepository.findByUsernameOrEmail(loginRequest.username())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(false);

        Assertions.assertThrows(BadCredentialsException.class, () -> {
            loginService.loginAndGenerateToken(loginRequest);
        });

        verify(userRepository, times(1)).findByUsernameOrEmail(loginRequest.username());
        verify(bCryptPasswordEncoder, times(1)).matches(loginRequest.password(), user.getPassword());
        verify(tokenService, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }
}