package br.com.casa_moreno.casa_moreno_backend.security;

import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TokenService Tests")
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;
    @InjectMocks
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(UUID.randomUUID())
                .name("Matteus Moreno")
                .username("matteus")
                .profile(Profile.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Should generate a token with correct claims and expiration")
    void shouldGenerateTokenWithCorrectClaims() {
        String expectedTokenValue = "mocked.jwt.token";
        Instant now = Instant.now();

        Jwt mockJwt = Jwt.withTokenValue(expectedTokenValue)
                .header("alg", "RS256")
                .subject(testUser.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60 * 60 * 24))
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String generatedToken = tokenService.generateToken(testUser);

        assertEquals(expectedTokenValue, generatedToken, "The generated token value should match the mocked one.");

        ArgumentCaptor<JwtEncoderParameters> paramsCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, times(1)).encode(paramsCaptor.capture());

        JwtClaimsSet capturedClaims = paramsCaptor.getValue().getClaims();
        assertAll("Verify JWT claims are set correctly",
                () -> assertEquals(testUser.getUsername(), capturedClaims.getSubject(), "Subject should be the username."),
                () -> assertEquals(testUser.getUserId().toString(), capturedClaims.getClaim("userId"), "userId claim should be correct."),
                () -> assertEquals("ADMIN", capturedClaims.getClaim("scope"), "Scope claim should match the user's profile."),
                () -> assertEquals(testUser.getName(), capturedClaims.getClaim("name"), "Name claim should be correct."),
                () -> assertNotNull(capturedClaims.getIssuedAt(), "IssuedAt claim should not be null."),
                () -> assertNotNull(capturedClaims.getExpiresAt(), "ExpiresAt claim should not be null."),
                () -> assertTrue(capturedClaims.getExpiresAt().isAfter(capturedClaims.getIssuedAt()), "Expiration should be after issuance time.")
        );
    }
}