package br.com.casa_moreno.casa_moreno_backend.login;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtEncoder jwtEncoder;


    public LoginService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponse generateToken(LoginRequest loginRequest) {
        String loginEmailOrUsername = loginRequest.username();

        User user = userRepository.findByUsernameOrEmail(loginEmailOrUsername, loginEmailOrUsername)
                .orElseThrow(() -> new BadCredentialsException("Username or password is invalid!"));

        if (!user.isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("Username or password is invalid!");
        }

        Instant expiresAt = Instant.now().plusSeconds(3600);
        var claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("user id", user.getUserId())
                .claim("scope", user.getProfile().name())
                .claim("email", user.getEmail())
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        LocalDateTime expiresTokenAt = LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault());
        String tokenJwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        user.setToken(tokenJwt);
        user.setExpiresTokenAt(expiresTokenAt);

        userRepository.save(user);

        return new LoginResponse(tokenJwt, expiresTokenAt);
    }
}
