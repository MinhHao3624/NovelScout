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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "novels")
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "novel_status", nullable = false, length = 32)
    private NovelStatus novelStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", nullable = false, length = 32)
    private PublicationStatus publicationStatus;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Column(name = "source_name", length = 64)
    private String sourceName;

    @Column(name = "source_license", length = 128)
    private String sourceLicense;

    @Column(name = "source_attribution_url", length = 512)
    private String sourceAttributionUrl;

    @Column(name = "imported_at")
    private Instant importedAt;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    private long ratingCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 24)
    @JoinTable(name = "novel_categories", joinColumns = @JoinColumn(name = "novel_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new LinkedHashSet<>();

    protected Novel() {}

    public static Novel published(String title, String slug, Author author, String description,
                                  NovelStatus status, long viewCount, BigDecimal rating,
                                  long ratingCount, Set<Category> categories) {
        Novel novel = new Novel();
        novel.title = title;
        novel.slug = slug;
        novel.author = author;
        novel.description = description;
        novel.novelStatus = status;
        novel.publicationStatus = PublicationStatus.PUBLISHED;
        novel.viewCount = viewCount;
        novel.averageRating = rating;
        novel.ratingCount = ratingCount;
        novel.publishedAt = Instant.now();
        novel.categories.addAll(categories);
        return novel;
    }

    public static Novel imported(String title, String slug, Author author, String description,
                                 String sourceUrl, String sourceName, String sourceLicense,
                                 Set<Category> categories) {
        Novel novel = new Novel();
        novel.title = title;
        novel.slug = slug;
        novel.author = author;
        novel.description = description;
        novel.novelStatus = NovelStatus.COMPLETED;
        novel.publicationStatus = PublicationStatus.PUBLISHED;
        novel.sourceUrl = sourceUrl;
        novel.sourceName = sourceName;
        novel.sourceLicense = sourceLicense;
        novel.sourceAttributionUrl = sourceUrl;
        novel.importedAt = Instant.now();
        novel.categories.addAll(categories);
        return novel;
    }

    public void updateImportedMetadata(String title, Author author, String description,
                                       String sourceName, String sourceLicense, Set<Category> categories) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.sourceName = sourceName;
        this.sourceLicense = sourceLicense;
        this.sourceAttributionUrl = sourceUrl;
        this.importedAt = Instant.now();
        this.publicationStatus = PublicationStatus.PUBLISHED;
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public void replaceCategories(Set<Category> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public Author getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public String getSourceUrl() { return sourceUrl; }
    public String getSourceName() { return sourceName; }
    public String getSourceLicense() { return sourceLicense; }
    public String getSourceAttributionUrl() { return sourceAttributionUrl; }
    public NovelStatus getNovelStatus() { return novelStatus; }
    public long getViewCount() { return viewCount; }
    public BigDecimal getAverageRating() { return averageRating; }
    public long getRatingCount() { return ratingCount; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<Category> getCategories() { return Collections.unmodifiableSet(categories); }
}
