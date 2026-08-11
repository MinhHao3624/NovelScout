package com.minhhao.novelscout.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "chapters")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "chapter_number", nullable = false, precision = 10, scale = 2)
    private BigDecimal chapterNumber;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "content_format", nullable = false, length = 16)
    private String contentFormat = "HTML";

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", nullable = false, length = 32)
    private PublicationStatus publicationStatus;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    protected Chapter() {}

    public static Chapter imported(Novel novel, String title, BigDecimal chapterNumber,
                                   String content, String sourceUrl) {
        Chapter chapter = new Chapter();
        chapter.novel = novel;
        chapter.title = title;
        chapter.chapterNumber = chapterNumber;
        chapter.content = content;
        chapter.contentFormat = "HTML";
        chapter.sourceUrl = sourceUrl;
        chapter.publicationStatus = PublicationStatus.PUBLISHED;
        chapter.publishedAt = Instant.now();
        return chapter;
    }

    public void updateImportedContent(String title, String content) {
        this.title = title;
        this.content = content;
        this.contentFormat = "HTML";
        this.publicationStatus = PublicationStatus.PUBLISHED;
    }

    public Long getId() { return id; }
    public Novel getNovel() { return novel; }
    public String getTitle() { return title; }
    public BigDecimal getChapterNumber() { return chapterNumber; }
    public String getContent() { return content; }
    public String getSourceUrl() { return sourceUrl; }
}
