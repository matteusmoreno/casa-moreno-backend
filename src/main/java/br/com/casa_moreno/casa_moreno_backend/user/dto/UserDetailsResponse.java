package br.com.casa_moreno.casa_moreno_backend.user.dto;

import br.com.casa_moreno.casa_moreno_backend.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailsResponse(
        UUID userId,
        String name,
        String username,
        String email,
        String phone,
        String profile,
        String profilePictureUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean active) {


    public UserDetailsResponse(User user) {
        this(
                user.getUserId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getProfile().toString(),
                user.getProfilePictureUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getActive()
        );
    }
}