package br.com.casa_moreno.casa_moreno_backend.user.service;

import br.com.casa_moreno.casa_moreno_backend.email.service.EmailService;
import br.com.casa_moreno.casa_moreno_backend.exception.PasswordResetTokenExpiredException;
import br.com.casa_moreno.casa_moreno_backend.exception.UserAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.UserNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.infra.StoragePort;
import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UpdateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UserDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("User Service Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private StoragePort storagePort;
    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private User savedUser;
    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest("User name", "username", "password", "user@email.com", "22999999999");
        savedUser = User.builder()
                .userId(UUID.randomUUID())
                .name(createUserRequest.name())
                .username(createUserRequest.username())
                .password("encodedPassword")
                .email(createUserRequest.email())
                .phone(createUserRequest.phone())
                .profile(Profile.USER)
                .profilePictureUrl(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        regularUser = User.builder()
                .userId(UUID.randomUUID())
                .name("Regular User")
                .username("regularuser")
                .password("encodedPassword")
                .email("user@email.com")
                .phone("22999999999")
                .profile(Profile.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .userId(UUID.randomUUID())
                .name("Admin User")
                .username("adminuser")
                .password("encodedAdminPassword")
                .email("admin@email.com")
                .profile(Profile.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void mockAuthenticatedUser(User user) {
        UserDetails userDetails = user;
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Should create a new user successfully without profile picture")
    void shouldCreateNewUserSuccessfullyWithoutProfilePicture() throws IOException {
        when(userRepository.findByUsernameOrEmail(createUserRequest.username())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailService).sendRegistrationConfirmationEmail(createUserRequest.email(), createUserRequest.name());

        User result = userService.createUser(createUserRequest, null);

        verify(userRepository, times(1)).findByUsernameOrEmail(createUserRequest.username());
        verify(bCryptPasswordEncoder, times(1)).encode(createUserRequest.password());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendRegistrationConfirmationEmail(anyString(), anyString());

        assertNull(savedUser.getProfilePictureUrl());

        assertAll(
                () -> assertNotNull(result.getUserId()),
                () -> assertEquals(createUserRequest.name(), result.getName()),
                () -> assertEquals(createUserRequest.username(), result.getUsername()),
                () -> assertEquals("encodedPassword", result.getPassword()),
                () -> assertEquals(createUserRequest.email(), result.getEmail()),
                () -> assertEquals(createUserRequest.phone(), result.getPhone()),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertNull(result.getProfilePictureUrl()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should create a new user successfully with profile picture")
    void shouldCreateNewUserWithProfilePicture() throws IOException {
        UUID userId = UUID.randomUUID();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "hello.jpg",
                "image/jpeg",
                "some-image-bytes".getBytes()
        );
        String expectedFileUrl = "http://example.com/profile-picture.jpg";

        User userAfterFirstSave = User.builder()
                .userId(userId)
                .name(createUserRequest.name())
                .username(createUserRequest.username())
                .password("encodedPassword")
                .email(createUserRequest.email())
                .phone(createUserRequest.phone())
                .profile(Profile.USER)
                .profilePictureUrl(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User userAfterSecondSave = User.builder()
                .userId(userId)
                .name(createUserRequest.name())
                .username(createUserRequest.username())
                .password("encodedPassword")
                .email(createUserRequest.email())
                .phone(createUserRequest.phone())
                .profile(Profile.USER)
                .profilePictureUrl(expectedFileUrl)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsernameOrEmail(createUserRequest.username())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(storagePort.uploadFile(any(), anyString(), anyString())).thenReturn(expectedFileUrl);
        when(userRepository.save(any(User.class)))
                .thenReturn(userAfterFirstSave)
                .thenReturn(userAfterSecondSave);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userAfterFirstSave));
        doNothing().when(emailService).sendRegistrationConfirmationEmail(anyString(), anyString());

        User result = userService.createUser(createUserRequest, mockFile);

        verify(userRepository, times(1)).findByUsernameOrEmail(createUserRequest.username());
        verify(bCryptPasswordEncoder, times(1)).encode(createUserRequest.password());
        verify(userRepository, times(2)).save(any(User.class));
        verify(emailService, times(1)).sendRegistrationConfirmationEmail(createUserRequest.email(), createUserRequest.name());
        verify(storagePort, times(1)).uploadFile(any(), anyString(), anyString());

        assertAll(
                () -> assertNotNull(result.getUserId()),
                () -> assertEquals(createUserRequest.name(), result.getName()),
                () -> assertEquals(createUserRequest.username(), result.getUsername()),
                () -> assertEquals("encodedPassword", result.getPassword()),
                () -> assertEquals(createUserRequest.email(), result.getEmail()),
                () -> assertEquals(createUserRequest.phone(), result.getPhone()),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertEquals("http://example.com/profile-picture.jpg", result.getProfilePictureUrl()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should create user and ignore an empty file")
    void shouldCreateUserAndIgnoreEmptyFile() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailService).sendRegistrationConfirmationEmail(anyString(), anyString());

        User result = userService.createUser(createUserRequest, emptyFile);

        verify(userRepository, times(1)).findByUsernameOrEmail(createUserRequest.username());
        verify(bCryptPasswordEncoder, times(1)).encode(createUserRequest.password());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendRegistrationConfirmationEmail(anyString(), anyString());
        verify(storagePort, never()).uploadFile(any(), anyString(), anyString());

        assertAll(
                () -> assertNotNull(result.getUserId()),
                () -> assertEquals(createUserRequest.name(), result.getName()),
                () -> assertEquals(createUserRequest.username(), result.getUsername()),
                () -> assertEquals("encodedPassword", result.getPassword()),
                () -> assertEquals(createUserRequest.email(), result.getEmail()),
                () -> assertEquals(createUserRequest.phone(), result.getPhone()),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertNull(result.getProfilePictureUrl(), "Profile picture URL should be null for an empty file."),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when creating user with existing username or email")
    void shouldThrowUserAlreadyExistsExceptionWhenCreatingUserWithExistingUsernameOrEmail() throws IOException {
        when(userRepository.findByUsernameOrEmail(createUserRequest.username()))
                .thenReturn(Optional.of(savedUser));

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(createUserRequest, null);
        });

        verify(userRepository, times(1)).findByUsernameOrEmail(createUserRequest.username());
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendRegistrationConfirmationEmail(anyString(), anyString());
        verify(storagePort, never()).uploadFile(any(), anyString(), anyString());

        assertEquals("Usuário ou e-mail já cadastrado.", exception.getMessage());
    }

    @Test
    @DisplayName("Should create user for OAuth correctly")
    void shouldCreateUserForOAuthCorrectly() {
        String userEmail = "michael_jackson@email.com";
        String userName = "Michael Jackson";

        User createdUser = User.builder()
                .userId(UUID.randomUUID())
                .name(userName)
                .email(userEmail)
                .username(userEmail)
                .password("hashedPassword")
                .profile(Profile.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(createdUser);
        doNothing().when(emailService).sendOAuthRegistrationWelcomeEmail(anyString(), anyString(), anyString());

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

        User result = userService.findOrCreateUserForOAuth(userEmail, userName);

        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(bCryptPasswordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendOAuthRegistrationWelcomeEmail(
                eq(userEmail),
                eq(userName),
                passwordCaptor.capture()
        );

        String capturedPassword = passwordCaptor.getValue();
        assertNotNull(capturedPassword);
        assertEquals(8, capturedPassword.length(), "Temporary password should be 8 characters long");

        assertAll(
                () -> assertNotNull(result.getUserId()),
                () -> assertEquals(userName, result.getName()),
                () -> assertEquals(userEmail, result.getUsername()),
                () -> assertEquals("hashedPassword", result.getPassword()),
                () -> assertEquals(userEmail, result.getEmail()),
                () -> assertNull(result.getPhone(), "Phone number should be null for OAuth users"),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertNull(result.getProfilePictureUrl()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should find user for OAuth and return existing user")
    void shouldFindUserForOAuthAndReturnExistingUser() {
        String userEmail = createUserRequest.email();
        String userName = createUserRequest.name();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(savedUser));

        User result = userService.findOrCreateUserForOAuth(userEmail, userName);
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendOAuthRegistrationWelcomeEmail(anyString(), anyString(), anyString());

        assertAll(
                () -> assertNotNull(result.getUserId()),
                () -> assertEquals(createUserRequest.name(), result.getName()),
                () -> assertEquals(createUserRequest.username(), result.getUsername()),
                () -> assertEquals("encodedPassword", result.getPassword()),
                () -> assertEquals(createUserRequest.email(), result.getEmail()),
                () -> assertEquals(createUserRequest.phone(), result.getPhone()),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertNull(result.getProfilePictureUrl()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should get user by username correctly")
    void shouldGetUserByUsernameCorrectly() {
        when(userRepository.findByUsername(savedUser.getUsername())).thenReturn(Optional.of(savedUser));

        User result = userService.getUserByUsername(savedUser.getUsername());

        verify(userRepository, times(1)).findByUsername(savedUser.getUsername());

        assertAll(
                () -> assertNotNull(result.getUserId()),
                () -> assertEquals(createUserRequest.name(), result.getName()),
                () -> assertEquals(createUserRequest.username(), result.getUsername()),
                () -> assertEquals("encodedPassword", result.getPassword()),
                () -> assertEquals(createUserRequest.email(), result.getEmail()),
                () -> assertEquals(createUserRequest.phone(), result.getPhone()),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertNull(result.getProfilePictureUrl()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by username")
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundByUsername() {
        String nonExistentUsername = "nonexistent_user";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByUsername(nonExistentUsername);
        });

        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
        assertEquals("User not found with username: " + nonExistentUsername, exception.getMessage());
    }

    @Test
    @DisplayName("Should find all users correctly")
    void shouldFindAllUsersCorrectly() {
        User user1 = User.builder()
                .userId(UUID.randomUUID())
                .name("User One")
                .username("userone")
                .password("encodedPassword1")
                .email("user1@email.com")
                .phone("22999999990")
                .profile(Profile.USER)
                .profilePictureUrl(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .userId(UUID.randomUUID())
                .name("User Two")
                .username("usertwo")
                .password("encodedPassword2")
                .email("user2@email.com")
                .phone("22999999991")
                .profile(Profile.USER)
                .profilePictureUrl(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDetailsResponse> result = userService.findAllUsers();

        verify(userRepository, times(1)).findAll();

        UserDetailsResponse userDetails1 = result.get(0);
        UserDetailsResponse userDetails2 = result.get(1);

        assertEquals(2, result.size());
        assertAll(
                () -> assertEquals(user1.getUserId(), userDetails1.userId()),
                () -> assertEquals(user1.getName(), userDetails1.name()),
                () -> assertEquals(user1.getUsername(), userDetails1.username()),
                () -> assertEquals(user1.getEmail(), userDetails1.email()),
                () -> assertEquals(user1.getPhone(), userDetails1.phone()),
                () -> assertEquals(user1.getProfile().name(), userDetails1.profile()),
                () -> assertNull(userDetails1.profilePictureUrl()),

                () -> assertEquals(user2.getUserId(), userDetails2.userId()),
                () -> assertEquals(user2.getName(), userDetails2.name()),
                () -> assertEquals(user2.getUsername(), userDetails2.username()),
                () -> assertEquals(user2.getEmail(), userDetails2.email()),
                () -> assertEquals(user2.getPhone(), userDetails2.phone()),
                () -> assertEquals(user2.getProfile().name(), userDetails2.profile()),
                () -> assertNull(userDetails2.profilePictureUrl())
        );
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    void shouldReturnEmptyListWhenNoUsersFound() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDetailsResponse> result = userService.findAllUsers();

        verify(userRepository, times(1)).findAll();

        assertTrue(result.isEmpty(), "Expected an empty list when no users are found");
    }

    @Test
    @DisplayName("Should update all user fields when all data is provided")
    void shouldUpdateAllUserFieldsWhenAllDataIsProvided() {
        UUID userId = savedUser.getUserId();
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                userId,
                "Updated Name",
                "updated_username",
                "new_password",
                "update_email@email.com",
                "22999999998");

        when(userRepository.findById(updateUserRequest.userId())).thenReturn(Optional.of(savedUser));
        when(bCryptPasswordEncoder.encode(updateUserRequest.password())).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.updateUser(updateUserRequest);

        verify(userRepository, times(1)).findById(updateUserRequest.userId());
        verify(bCryptPasswordEncoder, times(1)).encode(updateUserRequest.password());
        verify(userRepository, times(1)).save(any(User.class));

        assertAll(
                () -> assertEquals(updateUserRequest.userId(), result.getUserId()),
                () -> assertEquals(updateUserRequest.name(), result.getName()),
                () -> assertEquals(updateUserRequest.username(), result.getUsername()),
                () -> assertEquals("new_encoded_password", result.getPassword()),
                () -> assertEquals(updateUserRequest.email(), result.getEmail()),
                () -> assertEquals(updateUserRequest.phone(), result.getPhone()),
                () -> assertEquals("USER", result.getProfile().name()),
                () -> assertNull(result.getProfilePictureUrl()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNotNull(result.getUpdatedAt()),
                () -> assertNull(result.getPasswordResetToken()),
                () -> assertNull(result.getPasswordResetTokenExpiresAt()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    @DisplayName("Should allow user to update their own profile")
    void shouldAllowUserToUpdateOwnProfile() {
        mockAuthenticatedUser(regularUser);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                regularUser.getUserId(),
                "Updated Name",
                null,
                "new_password",
                "update_email@email.com",
                "22999999998");

        when(userRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
        when(bCryptPasswordEncoder.encode(updateUserRequest.password())).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(updateUserRequest);

        verify(userRepository, times(1)).findById(updateUserRequest.userId());
        verify(bCryptPasswordEncoder, times(1)).encode(updateUserRequest.password());
        verify(userRepository, times(1)).save(any(User.class));

        assertEquals("Updated Name", result.getName());
        assertEquals("update_email@email.com", result.getEmail());
        assertEquals("22999999998", result.getPhone());
    }

    @Test
    @DisplayName("Should allow admin to update another user's profile")
    void shouldAllowAdminToUpdateAnotherUserProfile() {
        mockAuthenticatedUser(adminUser);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                regularUser.getUserId(),
                null,
                "new_username", null, null, null);

        when(userRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(updateUserRequest);

        verify(userRepository, times(1)).findById(regularUser.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("new_username", result.getUsername());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when a user tries to update another user's profile")
    void shouldThrowAccessDeniedExceptionWhenUserTriesToUpdateAnotherUser() {
        mockAuthenticatedUser(regularUser);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                adminUser.getUserId(),
                "Attempt to Hack",
                null, null, null, null);

        when(userRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.updateUser(updateUserRequest);
        });

        assertEquals("You do not have permission to perform this action.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when security context is empty")
    void shouldThrowIllegalStateExceptionWhenSecurityContextIsEmpty() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(regularUser));
        SecurityContextHolder.clearContext();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.updateUser(new UpdateUserRequest(regularUser.getUserId(), "New Name", null, null, null, null));
        });

        assertTrue(exception.getMessage().contains("No authenticated user found"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when authentication is not authenticated")
    void shouldThrowIllegalStateExceptionWhenNotAuthenticated() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(regularUser));
        Authentication unauthenticated = mock(Authentication.class);

        when(unauthenticated.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(unauthenticated);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.updateUser(new UpdateUserRequest(regularUser.getUserId(), "New Name", null, null, null, null));
        });

        assertTrue(exception.getMessage().contains("No authenticated user found"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when principal is not an instance of UserDetails")
    void shouldThrowIllegalStateExceptionWhenPrincipalIsInvalid() {
        Authentication invalidPrincipalAuth = mock(Authentication.class);

        when(invalidPrincipalAuth.isAuthenticated()).thenReturn(true);
        when(invalidPrincipalAuth.getPrincipal()).thenReturn(new Object());

        SecurityContextHolder.getContext().setAuthentication(invalidPrincipalAuth);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(regularUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.updateUser(new UpdateUserRequest(regularUser.getUserId(), null, null, null, null, null));
        });

        assertTrue(exception.getMessage().contains("No authenticated user found"));
    }

    @Test
    @DisplayName("Should allow a user to delete their own profile")
    void shouldAllowUserToDeleteOwnProfile() {
        mockAuthenticatedUser(regularUser);

        when(userRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
        doNothing().when(userRepository).delete(regularUser);

        userService.deleteUserById(regularUser.getUserId());

        verify(userRepository, times(1)).findById(regularUser.getUserId());
        verify(userRepository, times(1)).delete(regularUser);
    }

    @Test
    @DisplayName("Should allow admin to delete another user")
    void shouldAllowAdminToDeleteAnotherUser() {
        mockAuthenticatedUser(adminUser);
        when(userRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
        doNothing().when(userRepository).delete(regularUser);

        assertDoesNotThrow(() -> {
            userService.deleteUserById(regularUser.getUserId());
        });

        verify(userRepository, times(1)).delete(regularUser);
    }

    @Test
    @DisplayName("Should NOT allow a user to delete another user's profile")
    void shouldNotAllowUserToDeleteAnotherUserProfile() {
        mockAuthenticatedUser(regularUser);

        when(userRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));

        assertThrows(AccessDeniedException.class, () -> {
            userService.deleteUserById(adminUser.getUserId());
        });

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void shouldThrowUserNotFoundExceptionWhenDeletingNonExistentUser() {
        UUID nonExistentUserId = UUID.randomUUID();

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUserById(nonExistentUserId);
        });
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(userRepository, never()).delete(any(User.class));

        assertEquals("User not found with id: " + nonExistentUserId, exception.getMessage());
    }

    @Test
    @DisplayName("Should generate password reset token correctly")
    void shouldGeneratePasswordResetTokenCorrectly() {
        String userEmail = savedUser.getEmail();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(savedUser));
        doNothing().when(emailService).sendPasswordResetLinkEmail(anyString(), anyString(), anyString());

        userService.generatePasswordResetToken(userEmail);

        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(userRepository, times(1)).save(savedUser); // Verifica se o objeto foi salvo
        verify(emailService, times(1)).sendPasswordResetLinkEmail(
                eq(userEmail),
                eq(savedUser.getName()),
                anyString()
        );

        assertAll(
                () -> assertNotNull(savedUser.getPasswordResetToken(), "Token should not be null"),
                () -> assertNotNull(savedUser.getPasswordResetTokenExpiresAt(), "Expiration date should not be null"),
                () -> assertTrue(savedUser.getPasswordResetTokenExpiresAt().isAfter(LocalDateTime.now()), "Expiration date should be in the future")
        );
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when generating password reset token for non-existent user")
    void shouldThrowUserNotFoundExceptionWhenGeneratingPasswordResetTokenForNonExistentUser() {
        String nonExistentEmail = "non_existent_email@email.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.generatePasswordResetToken(nonExistentEmail);
        });

        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetLinkEmail(anyString(), anyString(), anyString());

        assertEquals("User not found with email: " + nonExistentEmail, exception.getMessage());
    }

    @Test
    @DisplayName("Should reset password correctly")
    void shouldResetPasswordCorrectly() {
        String token = UUID.randomUUID().toString();
        String newPassword = "new_password";

        savedUser.setPasswordResetToken(token);
        savedUser.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(savedUser));
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn("new_encoded_password");
        doNothing().when(emailService).sendPasswordChangeConfirmationEmail(anyString(), anyString());

        userService.resetPassword(token, newPassword);

        verify(userRepository, times(1)).findByPasswordResetToken(token);
        verify(bCryptPasswordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(savedUser);
        verify(emailService, times(1)).sendPasswordChangeConfirmationEmail(
                eq(savedUser.getEmail()),
                eq(savedUser.getName())
        );

        assertAll(
                () -> assertEquals("new_encoded_password", savedUser.getPassword()),
                () -> assertNull(savedUser.getPasswordResetToken(), "Token should be cleared after reset"),
                () -> assertNull(savedUser.getPasswordResetTokenExpiresAt(), "Expiration date should be cleared after reset"),
                () -> assertNotNull(savedUser.getUpdatedAt(), "Updated at should be set")
        );
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when resetting password with invalid token")
    void shouldThrowUserNotFoundExceptionWhenResettingPasswordWithInvalidToken() {
        String invalidToken = "invalid_token";

        when(userRepository.findByPasswordResetToken(invalidToken)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.resetPassword(invalidToken, "new_password");
        });

        verify(userRepository, times(1)).findByPasswordResetToken(invalidToken);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordChangeConfirmationEmail(anyString(), anyString());

        assertEquals("Invalid password reset token.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw PasswordResetTokenExpiredException when resetting password with expired token")
    void shouldThrowPasswordResetTokenExpiredExceptionWhenResettingPasswordWithExpiredToken() {
        String expiredToken = UUID.randomUUID().toString();
        String newPassword = "new_password";

        savedUser.setPasswordResetToken(expiredToken);
        savedUser.setPasswordResetTokenExpiresAt(LocalDateTime.now().minusHours(1)); // Expired token

        when(userRepository.findByPasswordResetToken(expiredToken)).thenReturn(Optional.of(savedUser));

        Exception exception = assertThrows(PasswordResetTokenExpiredException.class, () -> {
            userService.resetPassword(expiredToken, newPassword);
        });

        verify(userRepository, times(1)).findByPasswordResetToken(expiredToken);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordChangeConfirmationEmail(anyString(), anyString());

        assertEquals("Password reset token has expired.", exception.getMessage());
    }

    @Test
    @DisplayName("Should upload profile picture for the first time")
    void shouldUploadProfilePictureForFirstTime() throws IOException {
        UUID userId = savedUser.getUserId();
        savedUser.setProfilePictureUrl(null);
        MockMultipartFile mockFile = new MockMultipartFile("file", "picture.jpg", "image/jpeg", "content".getBytes());
        String expectedUrl = "http://storage.com/profile-pictures/" + userId + ".jpg";

        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(storagePort.uploadFile(any(), anyString(), anyString())).thenReturn(expectedUrl);

        String resultUrl = userService.uploadProfilePicture(userId, mockFile);

        assertEquals(expectedUrl, resultUrl);
        verify(userRepository, times(1)).save(savedUser);
    }

    @Test
    @DisplayName("Should replace an existing profile picture")
    void shouldReplaceExistingProfilePicture() throws IOException {
        UUID userId = savedUser.getUserId();
        String oldFileUrl = "http://storage.com/old-picture.png";
        savedUser.setProfilePictureUrl(oldFileUrl); // User already has a profile picture

        MockMultipartFile newMockFile = new MockMultipartFile("file", "new_picture.jpg", "image/jpeg", "content".getBytes());
        String newExpectedUrl = "http://storage.com/new-picture.jpg";

        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(storagePort.uploadFile(any(), anyString(), anyString())).thenReturn(newExpectedUrl);
        doNothing().when(storagePort).deleteFile(anyString());

        String resultUrl = userService.uploadProfilePicture(userId, newMockFile);

        verify(userRepository, times(1)).findById(userId);
        verify(storagePort, times(1)).deleteFile(oldFileUrl);
        verify(storagePort, times(1)).uploadFile(any(), anyString(), anyString());
        verify(userRepository, times(1)).save(savedUser);

        assertEquals(newExpectedUrl, resultUrl, "Returned URL should be the new one");
        assertEquals(newExpectedUrl, savedUser.getProfilePictureUrl(), "User's profile picture URL should be updated to the new one");
    }

    @Test
    @DisplayName("Should not delete old file if its URL is an empty string")
    void shouldNotDeleteOldFileIfUrlIsAnEmptyString() throws IOException {
        mockAuthenticatedUser(savedUser);
        UUID userId = savedUser.getUserId();

        savedUser.setProfilePictureUrl("");

        MockMultipartFile mockFile = new MockMultipartFile("file", "picture.jpg", "image/jpeg", "content".getBytes());
        String expectedUrl = "http://storage.com/profile-pictures/" + userId + ".jpg";

        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(storagePort.uploadFile(any(), anyString(), anyString())).thenReturn(expectedUrl);

        String resultUrl = userService.uploadProfilePicture(userId, mockFile);

        verify(storagePort, never()).deleteFile(anyString());

        verify(storagePort, times(1)).uploadFile(any(), anyString(), anyString());
        verify(userRepository, times(1)).save(savedUser);

        assertEquals(expectedUrl, resultUrl);
        assertEquals(expectedUrl, savedUser.getProfilePictureUrl());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when uploading picture for a non-existent user")
    void shouldThrowUserNotFoundWhenUploadingPictureForNonExistentUser() {
        UUID nonExistentUserId = UUID.randomUUID();
        MockMultipartFile mockFile = new MockMultipartFile("file", "picture.jpg", "image/jpeg", "content".getBytes());

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.uploadProfilePicture(nonExistentUserId, mockFile);
        });

        verify(storagePort, never()).deleteFile(anyString());
        verify(storagePort, never()).uploadFile(any(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle file upload when filename has no extension")
    void shouldHandleFileUploadWhenFileNameHasNoExtension() throws IOException {
        mockAuthenticatedUser(savedUser);
        UUID userId = savedUser.getUserId();
        MockMultipartFile mockFile = new MockMultipartFile("file", "pictureWithoutExtension", "image/jpeg", "content".getBytes());

        String expectedFileName = "profile-pictures/" + userId + ".";
        String expectedReturnUrl = "http://storage.com/url_final";

        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        when(storagePort.uploadFile(any(), eq(expectedFileName), anyString())).thenReturn(expectedReturnUrl);

        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        String resultUrl = userService.uploadProfilePicture(userId, mockFile);

        verify(storagePort, times(1)).uploadFile(any(), fileNameCaptor.capture(), anyString());

        assertEquals(expectedFileName, fileNameCaptor.getValue());
        assertEquals(expectedReturnUrl, resultUrl);
    }

    @Test
    @DisplayName("Should handle file upload when getOriginalFilename returns null")
    void shouldHandleFileUploadWhenGetOriginalFilenameReturnsNull() throws IOException {
        UUID userId = savedUser.getUserId();

        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getBytes()).thenReturn("content".getBytes());
        when(mockFile.getContentType()).thenReturn("application/octet-stream");

        String expectedFileName = "profile-pictures/" + userId + ".";
        String expectedReturnUrl = "http://storage.com/some-url";

        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(storagePort.uploadFile(any(), eq(expectedFileName), anyString())).thenReturn(expectedReturnUrl);
        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        String resultUrl = userService.uploadProfilePicture(userId, mockFile);

        verify(storagePort, times(1)).uploadFile(any(), fileNameCaptor.capture(), anyString());

        assertEquals(expectedFileName, fileNameCaptor.getValue());
        assertEquals(expectedReturnUrl, resultUrl);
    }

    @Test
    @DisplayName("Should handle file upload when filename is empty")
    void shouldHandleFileUploadWhenFileNameIsEmpty() throws IOException {
        mockAuthenticatedUser(savedUser);
        UUID userId = savedUser.getUserId();
        MockMultipartFile mockFile = new MockMultipartFile("file", "", "image/jpeg", "content".getBytes());

        String expectedFileName = "profile-pictures/" + userId + ".";
        String expectedReturnUrl = "http://storage.com/url_final_empty";

        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        when(storagePort.uploadFile(any(), eq(expectedFileName), anyString())).thenReturn(expectedReturnUrl);

        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        String resultUrl = userService.uploadProfilePicture(userId, mockFile);

        verify(storagePort, times(1)).uploadFile(any(), fileNameCaptor.capture(), anyString());

        assertEquals(expectedFileName, fileNameCaptor.getValue());
        assertEquals(expectedReturnUrl, resultUrl);
    }

    @Test
    @DisplayName("Should load user by username correctly for Spring Security")
    void shouldLoadUserByUsernameCorrectly() {
        String username = savedUser.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(savedUser));

        var userDetails = userService.loadUserByUsername(username);

        verify(userRepository, times(1)).findByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when loading a non-existent user for Spring Security")
    void shouldThrowUserNotFoundExceptionWhenLoadingNonExistentUser() {
        String nonExistentUsername = "non_existent_user";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.loadUserByUsername(nonExistentUsername);
        });

        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
    }
}