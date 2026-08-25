package com.minhhao.novelscout.interaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByUserId(Long userId);
    List<UserInteraction> findByNovelId(Long novelId);
}
