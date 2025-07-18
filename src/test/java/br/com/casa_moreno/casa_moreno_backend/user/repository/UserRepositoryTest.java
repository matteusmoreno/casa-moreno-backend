package br.com.casa_moreno.casa_moreno_backend.user.repository;

import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Custom Query Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("Test User")
                .username("testuser")
                .password("password")
                .email("test@email.com")
                .profile(Profile.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find user by username using findByUsernameOrEmail")
    void shouldFindUserByUsername_whenUsingFindByUsernameOrEmail() {
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("testuser");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Should find user by email using findByUsernameOrEmail")
    void shouldFindUserByEmail_whenUsingFindByUsernameOrEmail() {
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("test@email.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should return empty optional when login does not match username or email")
    void shouldReturnEmpty_whenLoginIsIncorrect() {
        Optional<User> foundUser = userRepository.findByUsernameOrEmail("nonexistentlogin");

        assertThat(foundUser).isNotPresent();
    }
}