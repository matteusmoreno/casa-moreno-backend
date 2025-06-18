package br.com.casa_moreno.casa_moreno_backend.user.controller;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UserDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

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
}
