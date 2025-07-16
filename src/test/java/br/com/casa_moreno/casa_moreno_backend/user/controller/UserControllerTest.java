package br.com.casa_moreno.casa_moreno_backend.user.controller;

import br.com.casa_moreno.casa_moreno_backend.exception.GlobalExceptionHandler;
import br.com.casa_moreno.casa_moreno_backend.exception.PasswordResetTokenExpiredException;
import br.com.casa_moreno.casa_moreno_backend.exception.UserAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.UserNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.PasswordResetRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UpdateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UserDetailsResponse;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Tests")
class UserControllerTest {

    // PRECISA TESTAR OS ENDPOINTS COM AS ROLES DE USUÁRIO E ADMINISTRADOR

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        regularUser = User.builder()
                .userId(UUID.fromString("c0a8011c-c0d1-11ed-8a4a-0242ac120002"))
                .name("Regular User")
                .username("regularuser")
                .password("encodedPassword123")
                .email("regularuser@email.com")
                .phone("88888888888")
                .profile(Profile.USER)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        adminUser = User.builder()
                .userId(UUID.fromString("c0a8011c-c0d1-11ed-8a4a-0242ac120001"))
                .name("Admin User")
                .username("admin")
                .password("encodedAdminPassword123")
                .email("adminuser@email.com")
                .phone("99999999999")
                .profile(Profile.ADMIN)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create user without file and return 201 Created")
    void shouldCreateUserWithoutFileAndReturn201() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("Regular User", "regularuser", "password123", "regularuser@email.com", "88888888888");
        MockMultipartFile userJsonPart = new MockMultipartFile("user", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(createUserRequest).getBytes());

        when(userService.createUser(any(CreateUserRequest.class), isNull())).thenReturn(regularUser);

        mockMvc.perform(
                multipart("/users/create")
                        .file(userJsonPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(regularUser.getUserId().toString()))
                .andExpect(jsonPath("$.name").value(regularUser.getName()));
    }

    @Test
    @DisplayName("Should create user with file and return 201 Created")
    void shouldCreateUserWithFileAndReturn201() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("User With Pic", "userpic", "password123", "userpic@email.com", "11111111111");
        MockMultipartFile userJsonPart = new MockMultipartFile("user", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(createUserRequest).getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "some-image-bytes".getBytes());

        User userWithPic = User.builder().userId(UUID.randomUUID()).name("User With Pic").username("userpic").profile(Profile.USER).profilePictureUrl("http://example.com/pic.jpg").build();

        when(userService.createUser(any(CreateUserRequest.class), any(MockMultipartFile.class))).thenReturn(userWithPic);

        mockMvc.perform(
                multipart("/users/create")
                        .file(userJsonPart).file(filePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(userWithPic.getName()))
                .andExpect(jsonPath("$.profilePictureUrl").value(userWithPic.getProfilePictureUrl()));
    }

    @Test
    @DisplayName("Should not create user if already exists and return 409 Conflict")
    void shouldNotCreateUserIfAlreadyExistsAndReturn409Conflict() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("Existing User", "existinguser", "password123", "existing@email.com", "99999999999");
        MockMultipartFile userJsonPart = new MockMultipartFile("user", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(createUserRequest).getBytes());
        String errorMessage = "Usuário ou e-mail já cadastrado.";
        when(userService.createUser(any(CreateUserRequest.class), isNull())).thenThrow(new UserAlreadyExistsException(errorMessage));

        mockMvc.perform(
                multipart("/users/create")
                        .file(userJsonPart))
                .andExpect(status().isConflict())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @DisplayName("Should not create user with invalid request and return 400 Bad Request")
    void shouldNotCreateUserWithInvalidRequestAndReturn400BadRequest() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest("Invalid User", "invalid", "123", "invalid-email", "11111111111");
        MockMultipartFile userJsonPart = new MockMultipartFile("user", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(invalidRequest).getBytes());

        mockMvc.perform(
                multipart("/users/create")
                        .file(userJsonPart))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return User by username and return 200 OK")
    void shouldReturnUserByUsernameAndReturn200OK() throws Exception {
        String username = "regularuser";
        when(userService.getUserByUsername(username)).thenReturn(regularUser);

        mockMvc.perform(
                get("/users/username")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(regularUser.getUserId().toString()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when user by username does not exist")
    void shouldReturn404NotFoundWhenUserByUsernameDoesNotExist() throws Exception {
        String username = "nonexistentuser";
        String errorMessage = "User not found with username:" + username;
        when(userService.getUserByUsername(username)).thenThrow(new UserNotFoundException(errorMessage));

        mockMvc.perform(
                get("/users/username")
                        .param("username", username))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @DisplayName("Should return all users and return 200 OK")
    void shouldReturnAllUsersAndReturn200OK() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(new UserDetailsResponse(regularUser), new UserDetailsResponse(adminUser)));

        mockMvc.perform(
                get("/users/find-all-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(regularUser.getUsername()))
                .andExpect(jsonPath("$[1].username").value(adminUser.getUsername()));
    }

    @Test
    @DisplayName("Should update user and return 200 OK")
    void shouldUpdateUserAndReturn200OK() throws Exception {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(regularUser.getUserId(), "Updated User", "updateduser", "newpassword123", "new_email@email.com", "99999999999");
        User updatedUser = User.builder().userId(regularUser.getUserId()).name("Updated User").username("updateduser").profile(Profile.USER).build();

        when(userService.updateUser(any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(
                put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedUser.getName()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating non-existent user")
    void shouldReturn404NotFoundWhenUpdatingNonExistentUser() throws Exception {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(UUID.randomUUID(), "Non Existent", "nonexistent", "pw", "non@existent.com", "123");
        String errorMessage = "User not found with ID: " + updateUserRequest.userId();
        when(userService.updateUser(any(UpdateUserRequest.class))).thenThrow(new UserNotFoundException(errorMessage));

        mockMvc.perform(
                put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @DisplayName("Should delete user and return 204 No Content")
    void shouldDeleteUserAndReturn204NoContent() throws Exception {
        UUID userIdToDelete = regularUser.getUserId();
        doNothing().when(userService).deleteUserById(userIdToDelete);

        mockMvc.perform(
                delete("/users/delete")
                        .param("userId", userIdToDelete.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existent user")
    void shouldReturn404NotFoundWhenDeletingNonExistentUser() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        String errorMessage = "User not found with ID: " + nonExistentUserId;
        doThrow(new UserNotFoundException(errorMessage)).when(userService).deleteUserById(nonExistentUserId);

        mockMvc.perform(
                delete("/users/delete")
                        .param("userId", nonExistentUserId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @DisplayName("Should generate password reset token and return 200 OK")
    void shouldGeneratePasswordResetTokenAndReturn200OK() throws Exception {
        String email = regularUser.getEmail();

        doNothing().when(userService).generatePasswordResetToken(email);
        mockMvc.perform(
                post("/users/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Se um usuário com este e-mail existir, um link para redefinição de senha foi enviado."));

        verify(userService, times(1)).generatePasswordResetToken(email);
    }

    @Test
    @DisplayName("Should return 404 Not Found when generating password reset token for non-existent user")
    void shouldReturn404NotFoundWhenGeneratingPasswordResetTokenForNonExistentUser() throws Exception {
        String email = "nonexistentemail";

        String errorMessage = "User not found with email: " + email;
        doThrow(new UserNotFoundException(errorMessage)).when(userService).generatePasswordResetToken(email);
        mockMvc.perform(
                post("/users/forgot-password")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
        verify(userService, times(1)).generatePasswordResetToken(email);
    }

    @Test
    @DisplayName("Should reset password and return 200 OK")
    void shouldResetPasswordAndReturn200OK() throws Exception {
        String token = "valid-reset-token";
        String newPassword = "newPassword123";
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest(token, newPassword);

        doNothing().when(userService).resetPassword(token, newPassword);

        mockMvc.perform(
                post("/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isOk());

        verify(userService, times(1)).resetPassword(token, newPassword);
    }

    @Test
    @DisplayName("Should return 404 Not Found when resetting password with invalid token")
    void shouldReturn404NotFoundWhenResettingPasswordWithInvalidToken() throws Exception {
        String invalidToken = "invalid-reset-token";
        String newPassword = "newPassword123";
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest(invalidToken, newPassword);
        String errorMessage = "Invalid password reset token.";

        doThrow(new UserNotFoundException(errorMessage)).when(userService).resetPassword(invalidToken, newPassword);

        mockMvc.perform(
                        post("/users/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(userService, times(1)).resetPassword(invalidToken, newPassword);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when password reset token is expired")
    void shouldReturn401UnauthorizedWhenPasswordResetTokenIsExpired() throws Exception {
        String expiredToken = "expired-reset-token";
        String newPassword = "newPassword123";
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest(expiredToken, newPassword);
        String errorMessage = "Password reset token has expired.";

        doThrow(new PasswordResetTokenExpiredException(errorMessage)).when(userService).resetPassword(expiredToken, newPassword);

        mockMvc.perform(
                        post("/users/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(errorMessage));

        verify(userService, times(1)).resetPassword(expiredToken, newPassword);
    }

    @Test
    @DisplayName("Should upload profile picture and return 200 OK")
    void shouldUploadProfilePictureAndReturn200OK() throws Exception {
        UUID userId = regularUser.getUserId();
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-image-bytes".getBytes());
        String fileUrl = "http://example.com/profile.jpg";

        when(userService.uploadProfilePicture(userId, filePart)).thenReturn(fileUrl);

        mockMvc.perform(
                multipart("/users/{userId}/profile-picture", userId)
                        .file(filePart))
                .andExpect(status().isOk())
                .andExpect(content().string(fileUrl));

        verify(userService, times(1)).uploadProfilePicture(userId, filePart);
    }

    @Test
    @DisplayName("Should return 500 when service fails to upload profile picture")
    void shouldReturn500WhenServiceFailsToUploadProfilePicture() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-image-bytes".getBytes());

        String exceptionMessage = "User not found with id: " + nonExistentUserId;

        when(userService.uploadProfilePicture(eq(nonExistentUserId), any(MultipartFile.class)))
                .thenThrow(new IOException(exceptionMessage));

        String expectedResponseContent = "Erro ao fazer upload da imagem: " + exceptionMessage;

        mockMvc.perform(
                        multipart("/users/{userId}/profile-picture", nonExistentUserId)
                                .file(filePart))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(expectedResponseContent));
        verify(userService, times(1)).uploadProfilePicture(eq(nonExistentUserId), any(MultipartFile.class));
    }
}