package br.com.casa_moreno.casa_moreno_backend.login.controller;

import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginRequest;
import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginResponse;
import br.com.casa_moreno.casa_moreno_backend.login.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = loginService.loginAndGenerateToken(loginRequest);

        return ResponseEntity.ok(response);
    }
}
