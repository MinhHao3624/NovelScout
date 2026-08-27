CREATE TABLE novel_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_novel_comments_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE CASCADE,
    CONSTRAINT fk_novel_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_novel_comments_parent FOREIGN KEY (parent_id) REFERENCES novel_comments (id) ON DELETE CASCADE
);

CREATE INDEX idx_novel_comments_novel ON novel_comments (novel_id);
CREATE INDEX idx_novel_comments_parent ON novel_comments (parent_id);
