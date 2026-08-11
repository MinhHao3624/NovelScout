package com.minhhao.novelscout.crawler.wikisource;

import com.minhhao.novelscout.crawler.imports.CrawlImportItem;
import com.minhhao.novelscout.crawler.imports.CrawlImportItemRepository;
import com.minhhao.novelscout.crawler.imports.CrawlImportRun;
import com.minhhao.novelscout.crawler.imports.CrawlImportRunRepository;
import com.minhhao.novelscout.crawler.imports.ImportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WikisourceImportService {
    private static final String SOURCE_NAME = "WIKISOURCE_VI";
    private static final Logger log = LoggerFactory.getLogger(WikisourceImportService.class);

    private final WikisourceGateway gateway;
    private final WikisourceWorkImporter workImporter;
    private final CrawlImportRunRepository runRepository;
    private final CrawlImportItemRepository itemRepository;

    public WikisourceImportService(WikisourceGateway gateway, WikisourceWorkImporter workImporter,
                                   CrawlImportRunRepository runRepository,
                                   CrawlImportItemRepository itemRepository) {
        this.gateway = gateway;
        this.workImporter = workImporter;
        this.runRepository = runRepository;
        this.itemRepository = itemRepository;
    }

    public ImportReport importPages(List<String> pages) {
        CrawlImportRun run = runRepository.saveAndFlush(CrawlImportRun.start(SOURCE_NAME, pages.size()));
        for (int index = 0; index < pages.size(); index++) {
            String sourcePage = pages.get(index);
            log.info("Wikisource [{}/{}] đang xử lý: {}", index + 1, pages.size(), sourcePage);
            CrawlImportItem item = itemRepository.saveAndFlush(
                    CrawlImportItem.start(run, sourcePage, WikisourceParser.canonicalUrl(sourcePage)));
            try {
                WikisourceWorkData data = gateway.fetchWork(sourcePage);
                PersistedWork persisted = workImporter.persist(data);
                item.success(persisted.novel(), data.title(), persisted.chapterCount(), persisted.created());
                run.recordSuccess(persisted.created(), persisted.chapterCount());
                log.info("Wikisource [{}/{}] thành công: {} ({} chương, {})", index + 1, pages.size(),
                        data.title(), persisted.chapterCount(), persisted.created() ? "tạo mới" : "cập nhật");
            } catch (Exception exception) {
                String message = rootMessage(exception);
                item.fail(message);
                run.recordFailure(sourcePage, message);
                log.warn("Wikisource [{}/{}] thất bại: {} - {}", index + 1, pages.size(), sourcePage, message);
            }
            itemRepository.save(item);
            runRepository.save(run);
        }
        run.complete();
        return ImportReport.from(runRepository.save(run));
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
