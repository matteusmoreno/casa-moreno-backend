package br.com.casa_moreno.casa_moreno_backend.user.repository;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.username = :login OR u.email = :login")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);
}
