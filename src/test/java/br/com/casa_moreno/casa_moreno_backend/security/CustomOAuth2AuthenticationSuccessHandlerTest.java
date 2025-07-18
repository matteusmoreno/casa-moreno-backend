package br.com.casa_moreno.casa_moreno_backend.security;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CustomOAuth2AuthenticationSuccessHandler Tests")
@ExtendWith(MockitoExtension.class)
class CustomOAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenService tokenService;
    @Mock
    private UserService userService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Spy
    private RedirectStrategy redirectStrategy;
    @InjectMocks
    private CustomOAuth2AuthenticationSuccessHandler successHandler;

    private final String frontendUrl = "https://www.casa-moreno.com/auth/callback";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "frontendRedirectUri", frontendUrl);
        successHandler.setRedirectStrategy(redirectStrategy);
    }

    private OAuth2User createMockOAuth2User(String email, String name) {
        Map<String, Object> attributes = Map.of("email", email, "name", name);
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "email");
    }

    @Test
    @DisplayName("Should handle existing user and redirect with token")
    void shouldHandleExistingUserAndRedirectWithToken() throws IOException, ServletException {
        String userEmail = "existing.user@google.com";
        String userName = "Existing Google User";
        String expectedToken = "existing.user.token";

        OAuth2User oauth2User = createMockOAuth2User(userEmail, userName);
        when(authentication.getPrincipal()).thenReturn(oauth2User);

        User existingUser = User.builder().userId(UUID.randomUUID()).build();
        when(userService.findOrCreateUserForOAuth(userEmail, userName)).thenReturn(existingUser);
        when(tokenService.generateToken(existingUser)).thenReturn(expectedToken);
        doNothing().when(redirectStrategy).sendRedirect(any(), any(), anyString());

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService, times(1)).findOrCreateUserForOAuth(userEmail, userName);
        verify(tokenService, times(1)).generateToken(existingUser);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());

        assertEquals(frontendUrl + "?token=" + expectedToken, urlCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle new user and redirect with token")
    void shouldHandleNewUserAndRedirectWithToken() throws IOException, ServletException {
        String userEmail = "new.user@google.com";
        String userName = "New Google User";
        String expectedToken = "new.user.token";

        OAuth2User oauth2User = createMockOAuth2User(userEmail, userName);
        when(authentication.getPrincipal()).thenReturn(oauth2User);

        User newUser = User.builder().userId(UUID.randomUUID()).build();

        when(userService.findOrCreateUserForOAuth(userEmail, userName)).thenReturn(newUser);
        when(tokenService.generateToken(newUser)).thenReturn(expectedToken);
        doNothing().when(redirectStrategy).sendRedirect(any(), any(), anyString());

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService, times(1)).findOrCreateUserForOAuth(userEmail, userName);
        verify(tokenService, times(1)).generateToken(newUser);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());

        assertEquals(frontendUrl + "?token=" + expectedToken, urlCaptor.getValue());
    }

    @Test
    @DisplayName("Should throw exception when userService fails")
    void shouldPropagateExceptionWhenUserServiceFailsOnMissingEmail() throws IOException {
        String userName = "User Without Email";
        OAuth2User oauth2UserWithoutEmail = new DefaultOAuth2User(Collections.emptyList(), Map.of("name", userName), "name");

        when(authentication.getPrincipal()).thenReturn(oauth2UserWithoutEmail);
        when(userService.findOrCreateUserForOAuth(isNull(), eq(userName)))
                .thenThrow(new IllegalArgumentException("Email from OAuth2 provider cannot be null"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, authentication);
        });

        assertEquals("Email from OAuth2 provider cannot be null", exception.getMessage());

        verify(tokenService, never()).generateToken(any());
        verify(redirectStrategy, never()).sendRedirect(any(), any(), any());
    }
}