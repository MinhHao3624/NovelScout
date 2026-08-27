ALTER TABLE novel_comments ADD COLUMN chapter_id BIGINT NULL;
ALTER TABLE novel_comments ADD CONSTRAINT fk_novel_comments_chapter FOREIGN KEY (chapter_id) REFERENCES chapters (id) ON DELETE CASCADE;
CREATE INDEX idx_novel_comments_chapter ON novel_comments (chapter_id);
