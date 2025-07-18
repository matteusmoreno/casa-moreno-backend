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
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;
    private final StoragePort storagePort;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService, StoragePort storagePort) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
        this.storagePort = storagePort;
    }

    @Transactional
    public User createUser(CreateUserRequest request, MultipartFile file) throws IOException {
        userRepository.findByUsernameOrEmail(request.username()).ifPresent(user -> {
            throw new UserAlreadyExistsException("Usuário ou e-mail já cadastrado.");
        });

        User user = User.builder()
                .name(request.name())
                .username(request.username())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .email(request.email())
                .phone(request.phone())
                .profile(Profile.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        User savedUser = userRepository.save(user);

        // Se um arquivo foi enviado, faz o upload
        if (file != null && !file.isEmpty()) {
            String fileUrl = uploadProfilePicture(savedUser.getUserId(), file);
            savedUser.setProfilePictureUrl(fileUrl);
        }

        emailService.sendRegistrationConfirmationEmail(request.email(), request.name());

        return savedUser;
    }

    @Transactional
    public User findOrCreateUserForOAuth(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
            String hashedPassword = bCryptPasswordEncoder.encode(temporaryPassword);

            User newUser = User.builder()
                    .name(name)
                    .email(email)
                    .username(email)
                    .password(hashedPassword)
                    .profile(Profile.USER)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User savedUser = userRepository.save(newUser);

            emailService.sendOAuthRegistrationWelcomeEmail(savedUser.getEmail(), savedUser.getName(), temporaryPassword);

            return savedUser;
        });
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public List<UserDetailsResponse> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserDetailsResponse::new)
                .toList();
    }

    @Transactional
    public User updateUser(UpdateUserRequest request) throws AccessDeniedException {
        User userToUpdate = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        authorizeAdminOrOwner(userToUpdate.getUsername());

        if (request.name() != null) userToUpdate.setName(request.name());
        if (request.username() != null) userToUpdate.setUsername(request.username());
        if (request.password() != null) userToUpdate.setPassword(bCryptPasswordEncoder.encode(request.password()));
        if (request.email() != null) userToUpdate.setEmail(request.email());
        if (request.phone() != null) userToUpdate.setPhone(request.phone());

        userToUpdate.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(userToUpdate);
    }

    @Transactional
    public void deleteUserById(UUID userId) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        authorizeAdminOrOwner(userToDelete.getUsername());

        userRepository.delete(userToDelete);
    }

    @Transactional
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();

        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        emailService.sendPasswordResetLinkEmail(user.getEmail(), user.getName(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new UserNotFoundException("Invalid password reset token."));

        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException("Password reset token has expired.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        emailService.sendPasswordChangeConfirmationEmail(user.getEmail(), user.getName());
    }

    @Transactional
    public String uploadProfilePicture(UUID userId, MultipartFile file) throws IOException {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        String oldFileUrl = userToUpdate.getProfilePictureUrl();
        if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
            storagePort.deleteFile(oldFileUrl);
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = "profile-pictures/" + userId + "." + fileExtension;


        String fileUrl = storagePort.uploadFile(
                file.getBytes(),
                fileName,
                file.getContentType()
        );

        userToUpdate.setProfilePictureUrl(fileUrl);
        userRepository.save(userToUpdate);

        return fileUrl;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1);
    }

    private UserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("No authenticated user found in security context.");
        }
        return (UserDetails) authentication.getPrincipal();
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    private void authorizeAdminOrOwner(String ownerUsername) throws AccessDeniedException {
        UserDetails loggedInUser = getAuthenticatedUser();
        if (!isAdmin(loggedInUser) && !loggedInUser.getUsername().equals(ownerUsername)) {
            throw new AccessDeniedException("You do not have permission to perform this action.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}
