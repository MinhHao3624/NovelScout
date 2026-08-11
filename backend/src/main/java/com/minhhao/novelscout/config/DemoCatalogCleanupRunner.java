package com.minhhao.novelscout.config;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Order(200)
@ConditionalOnProperty(prefix = "app.demo-data", name = "cleanup-after-real-import", havingValue = "true")
public class DemoCatalogCleanupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoCatalogCleanupRunner.class);
    private static final String REAL_SOURCE = "WIKISOURCE_VI";
    private static final long MINIMUM_REAL_NOVELS = 20;
    private static final Set<String> DEMO_SLUGS = Set.of(
            "moc-kiem-thien-ha",
            "nguoi-gac-den-cuoi-pho",
            "ho-so-mua-den",
            "tram-sao-thu-muoi-ba",
            "tang-thu-trong-gio",
            "ngay-thanh-pho-quen-ten",
            "mat-ma-phong-404",
            "ke-du-hanh-qua-giac-mo"
    );

    private final NovelRepository novelRepository;

    public DemoCatalogCleanupRunner(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long realNovelCount = novelRepository.countBySourceName(REAL_SOURCE);
        if (realNovelCount < MINIMUM_REAL_NOVELS) {
            throw new IllegalStateException("Từ chối xóa demo: mới có " + realNovelCount
                    + " truyện Wikisource, yêu cầu tối thiểu " + MINIMUM_REAL_NOVELS);
        }

        List<Novel> demoNovels = novelRepository.findAllBySlugInAndSourceUrlIsNull(DEMO_SLUGS);
        novelRepository.deleteAllInBatch(demoNovels);
        log.info("DEMO_CLEANUP_REPORT realNovels={} deletedDemoNovels={}", realNovelCount, demoNovels.size());
    }
}
