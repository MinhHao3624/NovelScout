package com.minhhao.novelscout.auth;

import com.minhhao.novelscout.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String normalized = login.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(normalized, normalized)
                .map(CustomUserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại"));
    }
}
