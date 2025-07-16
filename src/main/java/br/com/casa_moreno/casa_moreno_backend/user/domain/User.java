package br.com.casa_moreno.casa_moreno_backend.user.domain;

import br.com.casa_moreno.casa_moreno_backend.login.dto.LoginRequest;
import br.com.casa_moreno.casa_moreno_backend.user.constant.Profile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    private String name;
    private String username;
    private String password;
    private String email;
    private String phone;
    @Enumerated(EnumType.STRING)
    private Profile profile;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiresAt;
    private Boolean active;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.profile.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public boolean isLoginCorrect(LoginRequest loginRequest, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.password);
    }
}
