//package br.com.casa_moreno.casa_moreno_backend.user.controller;
//
//import br.com.casa_moreno.casa_moreno_backend.security.SecurityConfig;
//import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
//import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
//import br.com.casa_moreno.casa_moreno_backend.user.service.UserService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.isNull;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@DisplayName("UserController Tests")
//@WebMvcTest(UserController.class)
//@Import(SecurityConfig.class)
//class UserControllerTest {
//
//    private final MockMvc mockMvc;
//    private final UserService userService;
//    private final ObjectMapper objectMapper;
//
//    @Autowired
//    public UserControllerTest(MockMvc mockMvc, @Mock UserService userService, ObjectMapper objectMapper) {
//        this.mockMvc = mockMvc;
//        this.userService = userService;
//        this.objectMapper = objectMapper;
//    }
//
//    private Jwt adminJwt;
//    private Jwt userJwt;
//
//    private User regularUser;
//
//    @BeforeEach
//    void setUp() {
//        Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
//        adminJwt = Jwt.withTokenValue("token")
//                .header("alg", "RS256")
//                .claim("userId", "123e4567-e89b-12d3-a456-426614174000")
//                .claim("scope", "ADMIN")
//                .claim("username", "admin")
//                .claim("name", "Admin User")
//                .issuedAt(Instant.now())
//                .expiresAt(expiresAt)
//                .build();
//
//        userJwt = Jwt.withTokenValue("token")
//                .header("alg", "RS256")
//                .claim("userId", "123e4567-e89b-12d3-a456-426614174001")
//                .claim("scope", "USER")
//                .claim("username", "regularuser")
//                .claim("name", "Regular User")
//                .issuedAt(Instant.now())
//                .expiresAt(expiresAt)
//                .build();
//
//        regularUser = User.builder()
//                .userId(UUID.randomUUID())
//                .name("Regular User")
//                .username("regularuser")
//                .password("password123")
//                .email("regularuser@email.com")
//                .phone("88888888888")
//                .profilePictureUrl(null)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(null)
//                .passwordResetToken(null)
//                .passwordResetTokenExpiresAt(null)
//                .active(true)
//                .build();
//    }
//
//    @Test
//    @DisplayName("Should create user without file and return 201 Created")
//    void shouldCreateUserWithoutFileAndReturn201() throws Exception {
//        CreateUserRequest createUserRequest = new CreateUserRequest("Regular User", "regularuser", "password123", "regularuser@email.com", "88888888888");
//
//        when(userService.createUser(createUserRequest), isNull()).thenReturn(regularUser);
//
//        // 3. Crie a parte JSON da requisição multipart
//        MockMultipartFile userJsonPart = new MockMultipartFile(
//                "user", // Nome do @RequestPart
//                "",
//                MediaType.APPLICATION_JSON_VALUE,
//                objectMapper.writeValueAsString(createUserRequest).getBytes()
//        );
//
//        // ACT & ASSERT
//        mockMvc.perform(
//                        // Use o construtor multipart
//                        multipart("/users/create")
//                                .file(userJsonPart) // Adiciona a parte JSON
//                        // Se o endpoint for protegido, adicione a autenticação
//                        // .with(jwt().jwt(adminJwt))
//                )
//                .andExpect(status().isCreated()) // Verifica o status HTTP 201
//                .andExpect(jsonPath("$.userId").value(regularUser.getUserId().toString()))
//                .andExpect(jsonPath("$.name").value("New User"));
//    }
//}