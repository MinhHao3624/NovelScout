CREATE TABLE novel_favorites (
    user_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, novel_id),
    CONSTRAINT fk_novel_favorites_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_novel_favorites_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE CASCADE
);

CREATE TABLE novel_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    score INT NOT NULL,
    review_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_novel_ratings_user_novel UNIQUE (user_id, novel_id),
    CONSTRAINT fk_novel_ratings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_novel_ratings_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE CASCADE
);

CREATE TABLE user_interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    interaction_type VARCHAR(32) NOT NULL,
    weight DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_interactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_interactions_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE CASCADE
);

CREATE INDEX idx_novel_ratings_novel ON novel_ratings (novel_id);
CREATE INDEX idx_user_interactions_user ON user_interactions (user_id);
CREATE INDEX idx_user_interactions_novel ON user_interactions (novel_id);
