ALTER TABLE novels ADD COLUMN source_name VARCHAR(64);
ALTER TABLE novels ADD COLUMN source_license VARCHAR(128);
ALTER TABLE novels ADD COLUMN source_attribution_url VARCHAR(512);
ALTER TABLE novels ADD COLUMN imported_at TIMESTAMP;

ALTER TABLE chapters
    ADD COLUMN content_format VARCHAR(16) NOT NULL DEFAULT 'HTML';

CREATE TABLE crawl_import_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    requested_count INT NOT NULL DEFAULT 0,
    imported_count INT NOT NULL DEFAULT 0,
    updated_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    total_chapters INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    error_summary TEXT
);

CREATE TABLE crawl_import_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id BIGINT NOT NULL,
    source_page VARCHAR(300) NOT NULL,
    source_url VARCHAR(512),
    title VARCHAR(300),
    status VARCHAR(32) NOT NULL,
    novel_id BIGINT,
    imported_chapter_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT uk_crawl_import_items_run_page UNIQUE (run_id, source_page),
    CONSTRAINT fk_crawl_import_items_run FOREIGN KEY (run_id) REFERENCES crawl_import_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_crawl_import_items_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE SET NULL
);

CREATE INDEX idx_crawl_import_runs_source_started ON crawl_import_runs (source_name, started_at);
CREATE INDEX idx_crawl_import_items_status ON crawl_import_items (run_id, status);

INSERT INTO categories (name, slug) VALUES ('Văn học Việt Nam', 'van-hoc-viet-nam');
INSERT INTO categories (name, slug) VALUES ('Tiểu thuyết', 'tieu-thuyet');
