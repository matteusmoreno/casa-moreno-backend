package br.com.casa_moreno.casa_moreno_backend.user.controller;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.PasswordResetRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UpdateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UserDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserDetailsResponse> createUser(@RequestBody @Valid CreateUserRequest request, UriComponentsBuilder uriBuilder) {
        User user = userService.createUser(request);
        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(user.getUserId()).toUri();
        return ResponseEntity.created(uri).body(new UserDetailsResponse(user));
    }

    @GetMapping("/username")
    public ResponseEntity<UserDetailsResponse> getUserByUsername(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(new UserDetailsResponse(user));
    }

    @GetMapping("/find-all-users")
    public ResponseEntity<List<UserDetailsResponse>> findAllUsers() {
        List<UserDetailsResponse> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDetailsResponse> updateUser(@RequestBody @Valid UpdateUserRequest request) {
        User user = userService.updateUser(request);
        return ResponseEntity.ok(new UserDetailsResponse(user));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINT ANTIGO ATUALIZADO PARA SOLICITAR O LINK ---
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.generatePasswordResetToken(email);
        return ResponseEntity.ok("Se um usuário com este e-mail existir, um link para redefinição de senha foi enviado.");
    }

    // --- NOVO ENDPOINT PARA REDEFINIR A SENHA ---
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
