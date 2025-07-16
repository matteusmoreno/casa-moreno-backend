package br.com.casa_moreno.casa_moreno_backend.login.service;

import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginRequest;
import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginResponse;
import br.com.casa_moreno.casa_moreno_backend.security.TokenService; // IMPORT
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;


@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    public LoginService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse loginAndGenerateToken(LoginRequest loginRequest) {
        String loginEmailOrUsername = loginRequest.username();

        User user = userRepository.findByUsernameOrEmail(loginEmailOrUsername, loginEmailOrUsername)
                .orElseThrow(() -> new BadCredentialsException("Username or email is incorrect"));

        if (!user.isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("Username or password is incorrect");
        }

        String tokenJwt = tokenService.generateToken(user);


        Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
        LocalDateTime expiresTokenAt = LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault());

        userRepository.save(user);

        return new LoginResponse(tokenJwt, expiresTokenAt);
    }
}