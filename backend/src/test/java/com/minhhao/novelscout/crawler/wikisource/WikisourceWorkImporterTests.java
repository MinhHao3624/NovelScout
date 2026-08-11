package com.minhhao.novelscout.crawler.wikisource;

import com.minhhao.novelscout.catalog.AuthorRepository;
import com.minhhao.novelscout.catalog.ChapterRepository;
import com.minhhao.novelscout.catalog.NovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WikisourceWorkImporterTests {
    @Autowired private WikisourceWorkImporter importer;
    @Autowired private NovelRepository novelRepository;
    @Autowired private ChapterRepository chapterRepository;
    @Autowired private AuthorRepository authorRepository;

    @BeforeEach
    void cleanCatalog() {
        chapterRepository.deleteAll();
        novelRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void importingSameWorkTwiceIsIdempotent() {
        WikisourceWorkData data = new WikisourceWorkData(
                "Tắt đèn", "https://vi.wikisource.org/wiki/Tat", "Tắt đèn", "Ngô Tất Tố",
                "Một tác phẩm văn học hiện thực.", "PUBLIC_DOMAIN", Set.of("Tiểu_thuyết"),
                List.of(
                        new WikisourceChapterData("Tắt đèn/I", "I", "https://vi.wikisource.org/wiki/Tat/I", "<p>Chương một</p>"),
                        new WikisourceChapterData("Tắt đèn/II", "II", "https://vi.wikisource.org/wiki/Tat/II", "<p>Chương hai</p>")
                ));

        PersistedWork first = importer.persist(data);
        PersistedWork second = importer.persist(data);

        assertThat(first.created()).isTrue();
        assertThat(second.created()).isFalse();
        assertThat(novelRepository.count()).isEqualTo(1);
        assertThat(chapterRepository.count()).isEqualTo(2);
        assertThat(chapterRepository.countByNovelId(second.novel().getId())).isEqualTo(2);
        assertThat(second.novel().getCategories()).extracting(category -> category.getSlug())
                .contains("hien-thuc-xa-hoi", "gia-dinh", "than-phan-phu-nu", "nong-thon")
                .doesNotContain("tieu-thuyet");
    }
}
