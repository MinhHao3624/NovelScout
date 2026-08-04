package com.minhhao.novelscout.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);
}
