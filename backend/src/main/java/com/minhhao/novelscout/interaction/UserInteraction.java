package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_interactions")
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(name = "interaction_type", nullable = false, length = 32)
    private String interactionType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected UserInteraction() {}

    public UserInteraction(User user, Novel novel, String interactionType, BigDecimal weight) {
        this.user = user;
        this.novel = novel;
        this.interactionType = interactionType;
        this.weight = weight;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Novel getNovel() { return novel; }
    public String getInteractionType() { return interactionType; }
    public BigDecimal getWeight() { return weight; }
    public Instant getCreatedAt() { return createdAt; }
}
