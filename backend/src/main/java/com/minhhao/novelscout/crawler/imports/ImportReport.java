package com.minhhao.novelscout.crawler.imports;

public record ImportReport(Long runId, String status, int requested, int imported, int updated,
                           int skipped, int failed, int chapters, String errorSummary) {
    public static ImportReport from(CrawlImportRun run) {
        return new ImportReport(run.getId(), run.getStatus().name(), run.getRequestedCount(),
                run.getImportedCount(), run.getUpdatedCount(), run.getSkippedCount(),
                run.getFailedCount(), run.getTotalChapters(), run.getErrorSummary());
    }
}
