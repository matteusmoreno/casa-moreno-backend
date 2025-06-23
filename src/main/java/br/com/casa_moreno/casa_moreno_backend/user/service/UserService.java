package br.com.casa_moreno.casa_moreno_backend.user.service;

import br.com.casa_moreno.casa_moreno_backend.email.service.EmailService;
import br.com.casa_moreno.casa_moreno_backend.exception.UserNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import br.com.casa_moreno.casa_moreno_backend.user.dto.CreateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UpdateUserRequest;
import br.com.casa_moreno.casa_moreno_backend.user.dto.UserDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
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

        emailService.sendRegistrationConfirmationEmail(request.email(), request.name());

        return userRepository.save(user);
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
    public User updateUser(UpdateUserRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        if (request.name() != null) user.setName(request.name());
        if (request.username() != null) user.setUsername(request.username());
        if (request.password() != null) user.setPassword(bCryptPasswordEncoder.encode(request.password()));
        if (request.email() != null) user.setEmail(request.email());
        if (request.phone() != null) user.setPhone(request.phone());

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}
