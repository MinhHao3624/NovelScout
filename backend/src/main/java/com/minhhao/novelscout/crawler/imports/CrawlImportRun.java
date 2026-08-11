package com.minhhao.novelscout.crawler.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "crawl_import_runs")
public class CrawlImportRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 64)
    private String sourceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ImportRunStatus status;

    @Column(name = "requested_count", nullable = false)
    private int requestedCount;

    @Column(name = "imported_count", nullable = false)
    private int importedCount;

    @Column(name = "updated_count", nullable = false)
    private int updatedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "total_chapters", nullable = false)
    private int totalChapters;

    @Column(name = "started_at", nullable = false, insertable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_summary", columnDefinition = "TEXT")
    private String errorSummary;

    protected CrawlImportRun() {}

    public static CrawlImportRun start(String sourceName, int requestedCount) {
        CrawlImportRun run = new CrawlImportRun();
        run.sourceName = sourceName;
        run.requestedCount = requestedCount;
        run.status = ImportRunStatus.RUNNING;
        return run;
    }

    public void recordSuccess(boolean created, int chapters) {
        if (created) importedCount++; else updatedCount++;
        totalChapters += chapters;
    }

    public void recordFailure(String sourcePage, String message) {
        failedCount++;
        if (errorSummary == null || errorSummary.length() < 3500) {
            String next = sourcePage + ": " + message;
            errorSummary = errorSummary == null ? next : errorSummary + "\n" + next;
        }
    }

    public void recordSkipped() { skippedCount++; }

    public void complete() {
        completedAt = Instant.now();
        int succeeded = importedCount + updatedCount;
        status = failedCount == 0 ? ImportRunStatus.SUCCESS
                : succeeded == 0 ? ImportRunStatus.FAILED : ImportRunStatus.PARTIAL;
    }

    public Long getId() { return id; }
    public ImportRunStatus getStatus() { return status; }
    public int getRequestedCount() { return requestedCount; }
    public int getImportedCount() { return importedCount; }
    public int getUpdatedCount() { return updatedCount; }
    public int getSkippedCount() { return skippedCount; }
    public int getFailedCount() { return failedCount; }
    public int getTotalChapters() { return totalChapters; }
    public String getErrorSummary() { return errorSummary; }
}
