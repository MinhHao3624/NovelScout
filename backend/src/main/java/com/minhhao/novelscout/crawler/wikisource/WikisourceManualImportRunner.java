package com.minhhao.novelscout.crawler.wikisource;

import com.minhhao.novelscout.crawler.imports.ImportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(100)
@ConditionalOnExpression("${app.crawler.enabled:false} and ${app.crawler.wikisource.import-on-startup:false}")
public class WikisourceManualImportRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(WikisourceManualImportRunner.class);

    private final WikisourceManifestLoader manifestLoader;
    private final WikisourceImportService importService;
    private final int maxWorks;

    public WikisourceManualImportRunner(WikisourceManifestLoader manifestLoader,
                                        WikisourceImportService importService,
                                        @Value("${app.crawler.wikisource.max-works:0}") int maxWorks) {
        this.manifestLoader = manifestLoader;
        this.importService = importService;
        this.maxWorks = Math.max(0, maxWorks);
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> pages = manifestLoader.load();
        if (maxWorks > 0) pages = pages.stream().limit(maxWorks).toList();
        log.info("Bắt đầu import thủ công {} tác phẩm từ Wikisource tiếng Việt", pages.size());
        ImportReport report = importService.importPages(pages);
        log.info("WIKISOURCE_IMPORT_REPORT runId={} status={} requested={} imported={} updated={} failed={} chapters={}",
                report.runId(), report.status(), report.requested(), report.imported(), report.updated(),
                report.failed(), report.chapters());
        if (report.errorSummary() != null) log.warn("Wikisource import errors:\n{}", report.errorSummary());
    }
}
