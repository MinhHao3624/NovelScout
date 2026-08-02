CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(150),
    avatar_url VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_authors_slug UNIQUE (slug)
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE novels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    slug VARCHAR(300) NOT NULL,
    author_id BIGINT,
    description TEXT,
    cover_url VARCHAR(500),
    novel_status VARCHAR(32) NOT NULL DEFAULT 'ONGOING',
    publication_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    source_url VARCHAR(512),
    view_count BIGINT NOT NULL DEFAULT 0,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    rating_count BIGINT NOT NULL DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_novels_slug UNIQUE (slug),
    CONSTRAINT uk_novels_source_url UNIQUE (source_url),
    CONSTRAINT fk_novels_author FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE SET NULL
);

CREATE TABLE novel_categories (
    novel_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (novel_id, category_id),
    CONSTRAINT fk_novel_categories_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE CASCADE,
    CONSTRAINT fk_novel_categories_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

CREATE TABLE chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    chapter_number DECIMAL(10, 2) NOT NULL,
    content LONGTEXT NOT NULL,
    source_url VARCHAR(512),
    publication_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    view_count BIGINT NOT NULL DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chapters_novel_number UNIQUE (novel_id, chapter_number),
    CONSTRAINT uk_chapters_source_url UNIQUE (source_url),
    CONSTRAINT fk_chapters_novel FOREIGN KEY (novel_id) REFERENCES novels (id) ON DELETE CASCADE
);

CREATE INDEX idx_novels_author ON novels (author_id);
CREATE INDEX idx_novels_publication_status_updated ON novels (publication_status, updated_at);
CREATE INDEX idx_novels_title ON novels (title);
CREATE INDEX idx_chapters_novel_publication ON chapters (novel_id, publication_status, chapter_number);
