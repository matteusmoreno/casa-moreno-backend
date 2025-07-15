package br.com.casa_moreno.casa_moreno_backend.user.controller;

import br.com.casa_moreno.casa_moreno_backend.exception.GlobalExceptionHandler;
import br.com.casa_moreno.casa_moreno_backend.exception.UserAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.security.SecurityConfig;
import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserController Tests")
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private Jwt adminJwt;
    private Jwt userJwt;
    private User regularUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Register JavaTimeModule if your DTO uses LocalDateTime!
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        Instant expiresAt = Instant.now().plusSeconds(60 * 60 * 24);
        adminJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("userId", "123e4567-e89b-12d3-a456-426614174000")
                .claim("scope", "ADMIN")
                .claim("username", "admin")
                .claim("name", "Admin User")
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        userJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("userId", "123e4567-e89b-12d3-a456-426614174001")
                .claim("scope", "USER")
                .claim("username", "regularuser")
                .claim("name", "Regular User")
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        regularUser = User.builder()
                .userId(UUID.fromString("c0a8011c-c0d1-11ed-8a4a-0242ac120002"))
                .name("Regular User")
                .username("regularuser")
                .password("encodedPassword123")
                .email("regularuser@email.com")
                .phone("88888888888")
                .profile(Profile.USER)
                .profilePictureUrl(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .passwordResetToken(null)
                .passwordResetTokenExpiresAt(null)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create user without file and return 201 Created")
    void shouldCreateUserWithoutFileAndReturn201() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("Regular User", "regularuser", "password123", "regularuser@email.com", "88888888888");
        MockMultipartFile userJsonPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(createUserRequest).getBytes()
        );

        when(userService.createUser(any(CreateUserRequest.class), isNull())).thenReturn(regularUser);

        mockMvc.perform(
                        multipart("/users/create")
                                .file(userJsonPart)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(regularUser.getUserId().toString()))
                .andExpect(jsonPath("$.name").value(regularUser.getName()))
                .andExpect(jsonPath("$.username").value(regularUser.getUsername()))
                .andExpect(jsonPath("$.email").value(regularUser.getEmail()));
    }

    @Test
    @DisplayName("Should create user with file and return 201 Created")
    void shouldCreateUserWithFileAndReturn201() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("User With Pic", "userpic", "password123", "userpic@email.com", "11111111111");
        MockMultipartFile userJsonPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(createUserRequest).getBytes()
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-image-bytes".getBytes()
        );

        User userWithPic = User.builder()
                .userId(UUID.fromString("c0a8011c-c0d1-11ed-8a4a-0242ac120003"))
                .name("User With Pic")
                .username("userpic")
                .password("encodedPassword123")
                .email("userpic@email.com")
                .phone("11111111111")
                .profile(Profile.USER)
                .profilePictureUrl("http://example.com/profile-pictures/c0a8011c-c0d1-11ed-8a4a-0242ac120003.jpg")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        when(userService.createUser(any(CreateUserRequest.class), any(MockMultipartFile.class))).thenReturn(userWithPic);

        mockMvc.perform(
                        multipart("/users/create")
                                .file(userJsonPart)
                                .file(filePart)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userWithPic.getUserId().toString()))
                .andExpect(jsonPath("$.name").value(userWithPic.getName()))
                .andExpect(jsonPath("$.profilePictureUrl").value(userWithPic.getProfilePictureUrl()));
    }

    @Test
    @DisplayName("Should not create user if already exists and return 409 Conflict")
    void shouldNotCreateUserIfAlreadyExistsAndReturn409Conflict() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("Existing User", "existinguser", "password123", "existing@email.com", "99999999999");
        MockMultipartFile userJsonPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(createUserRequest).getBytes()
        );

        when(userService.createUser(any(CreateUserRequest.class), isNull()))
                .thenThrow(new UserAlreadyExistsException("Usuário ou e-mail já cadastrado."));

        mockMvc.perform(
                        multipart("/users/create")
                                .file(userJsonPart)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Usuário ou e-mail já cadastrado."));
    }

    @Test
    @DisplayName("Should not create user with invalid request and return 400 Bad Request")
    void shouldNotCreateUserWithInvalidRequestAndReturn400BadRequest() throws Exception {
        // Request with invalid email and too short password (assuming @Email and @Size min = 6 in DTO)
        CreateUserRequest invalidRequest = new CreateUserRequest("Invalid User", "invalid", "123", "invalid-email", "11111111111");
        MockMultipartFile userJsonPart = new MockMultipartFile(
                "user",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(invalidRequest).getBytes()
        );

        mockMvc.perform(
                        multipart("/users/create")
                                .file(userJsonPart)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").exists()); // Check if there is an error message in the body
        // You might want to be more specific here depending on your error response format
    }
}