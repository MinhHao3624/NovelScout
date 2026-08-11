package com.minhhao.novelscout.crawler.imports;

import com.minhhao.novelscout.catalog.Novel;
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

import java.time.Instant;

@Entity
@Table(name = "crawl_import_items")
public class CrawlImportItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CrawlImportRun run;

    @Column(name = "source_page", nullable = false, length = 300)
    private String sourcePage;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Column(length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ImportItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    private Novel novel;

    @Column(name = "imported_chapter_count", nullable = false)
    private int importedChapterCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false, insertable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected CrawlImportItem() {}

    public static CrawlImportItem start(CrawlImportRun run, String sourcePage, String sourceUrl) {
        CrawlImportItem item = new CrawlImportItem();
        item.run = run;
        item.sourcePage = sourcePage;
        item.sourceUrl = sourceUrl;
        item.status = ImportItemStatus.RUNNING;
        return item;
    }

    public void success(Novel novel, String title, int chapters, boolean created) {
        this.novel = novel;
        this.title = title;
        this.importedChapterCount = chapters;
        this.status = created ? ImportItemStatus.IMPORTED : ImportItemStatus.UPDATED;
        this.completedAt = Instant.now();
    }

    public void fail(String message) {
        this.status = ImportItemStatus.FAILED;
        this.errorMessage = message.length() > 4000 ? message.substring(0, 4000) : message;
        this.completedAt = Instant.now();
    }
}
