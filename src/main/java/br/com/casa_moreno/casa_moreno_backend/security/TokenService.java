package br.com.casa_moreno.casa_moreno_backend.security;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {
        Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24); // Token válido por 24 horas

         JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId().toString())
                .claim("scope", user.getProfile().name())
                .claim("name", user.getName())
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}