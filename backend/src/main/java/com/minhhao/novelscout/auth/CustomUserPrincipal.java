package com.minhhao.novelscout.auth;

import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record CustomUserPrincipal(Long id, String email, String username, String password,
                                  boolean enabled, List<GrantedAuthority> authorities) implements UserDetails {
    public static CustomUserPrincipal from(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .map(GrantedAuthority.class::cast)
                .toList();
        return new CustomUserPrincipal(user.getId(), user.getEmail(), user.getUsername(),
                user.getPasswordHash(), user.getStatus() == UserStatus.ACTIVE, authorities);
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isEnabled() { return enabled; }
}
