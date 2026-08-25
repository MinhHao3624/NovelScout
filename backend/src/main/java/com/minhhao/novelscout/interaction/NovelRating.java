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
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "novel_ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_novel_ratings_user_novel", columnNames = {"user_id", "novel_id"})
})
public class NovelRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false)
    private int score;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected NovelRating() {}

    public NovelRating(User user, Novel novel, int score, String reviewText) {
        this.user = user;
        this.novel = novel;
        this.score = score;
        this.reviewText = reviewText;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateRating(int score, String reviewText) {
        this.score = score;
        this.reviewText = reviewText;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Novel getNovel() { return novel; }
    public int getScore() { return score; }
    public String getReviewText() { return reviewText; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
