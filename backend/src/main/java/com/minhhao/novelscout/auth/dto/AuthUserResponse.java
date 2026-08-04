package com.minhhao.novelscout.auth.dto;

import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.User;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record AuthUserResponse(Long id, String email, String username, String displayName,
                               String avatarUrl, Set<String> roles, Instant createdAt) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getDisplayName(),
                user.getAvatarUrl(), user.getRoles().stream().map(Role::getName).map(Enum::name)
                        .collect(Collectors.toSet()), user.getCreatedAt());
    }
}
