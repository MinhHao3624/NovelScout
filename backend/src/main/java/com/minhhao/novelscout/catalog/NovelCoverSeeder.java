package com.minhhao.novelscout.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NovelCoverSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NovelCoverSeeder.class);
    private final NovelRepository novelRepository;

    public NovelCoverSeeder(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Clear previous AI generated cover URLs
        novelRepository.updateCoverUrl("tat-den", null);
        novelRepository.updateCoverUrl("chua-tau-kim-quy", null);

        // Set authentic user-provided covers
        novelRepository.updateCoverUrl("con-nha-giau", "/covers/con-nha-giau.png");
        novelRepository.updateCoverUrl("leu-chong-muc-luc", "/covers/leu-chong.png");
        novelRepository.updateCoverUrl("leu-chong", "/covers/leu-chong.png");
        novelRepository.updateCoverUrl("vo-quyt-day-mong-tay-nhon", "/covers/vo-quyt-day-mong-tay-nhon.png");
        novelRepository.updateCoverUrl("tu-hon", "/covers/tu-hon.png");
        novelRepository.updateCoverUrl("tro-vo-lua-ra", "/covers/tro-vo-lua-ra.png");
        novelRepository.updateCoverUrl("thay-chung-trung-so", "/covers/thay-chung-trung-so.png");
        novelRepository.updateCoverUrl("tien-bac-bac-tien", "/covers/tien-bac-bac-tien.png");
        novelRepository.updateCoverUrl("tai-mang-tuong-do", "/covers/tai-mang-tuong-do.png");
        novelRepository.updateCoverUrl("phong-tran-tham-su", "/covers/phong-tran-tham-su.png");

        log.info("Đã đồng bộ ảnh bìa: Đặt ảnh bìa người dùng cho 9 tác phẩm.");
    }
}
