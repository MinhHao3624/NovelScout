package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.catalog.Chapter;
import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "novel_comments")
public class NovelComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private NovelComment parent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected NovelComment() {}

    public NovelComment(Novel novel, Chapter chapter, User user, NovelComment parent, String content) {
        this.novel = novel;
        this.chapter = chapter;
        this.user = user;
        this.parent = parent;
        this.content = content;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Novel getNovel() {
        return novel;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public User getUser() {
        return user;
    }

    public NovelComment getParent() {
        return parent;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
