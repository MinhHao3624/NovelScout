package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "novel_favorites")
public class NovelFavorite {

    @EmbeddedId
    private FavoriteId id = new FavoriteId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("novelId")
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected NovelFavorite() {}

    public NovelFavorite(User user, Novel novel) {
        this.user = user;
        this.novel = novel;
        this.id = new FavoriteId(user.getId(), novel.getId());
        this.createdAt = Instant.now();
    }

    public FavoriteId getId() { return id; }
    public User getUser() { return user; }
    public Novel getNovel() { return novel; }
    public Instant getCreatedAt() { return createdAt; }

    @Embeddable
    public static class FavoriteId implements Serializable {
        private Long userId;
        private Long novelId;

        public FavoriteId() {}
        public FavoriteId(Long userId, Long novelId) {
            this.userId = userId;
            this.novelId = novelId;
        }

        public Long getUserId() { return userId; }
        public Long getNovelId() { return novelId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FavoriteId that = (FavoriteId) o;
            return userId.equals(that.userId) && novelId.equals(that.novelId);
        }

        @Override
        public int hashCode() {
            return 31 * userId.hashCode() + novelId.hashCode();
        }
    }
}
