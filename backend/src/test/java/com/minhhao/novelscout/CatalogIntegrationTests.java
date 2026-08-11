package com.minhhao.novelscout;

import com.minhhao.novelscout.catalog.Author;
import com.minhhao.novelscout.catalog.AuthorRepository;
import com.minhhao.novelscout.catalog.Category;
import com.minhhao.novelscout.catalog.CategoryRepository;
import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.catalog.NovelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private NovelRepository novelRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private CategoryRepository categoryRepository;

    @BeforeEach
    void prepareCatalog() {
        novelRepository.deleteAll();
        authorRepository.deleteAll();
        Category mystery = categoryRepository.findBySlug("trinh-tham").orElseThrow();
        Category socialRealism = categoryRepository.findBySlug("hien-thuc-xa-hoi").orElseThrow();
        Author author = authorRepository.save(new Author("Minh Dư", "minh-du", "Tác giả thử nghiệm"));
        novelRepository.save(Novel.published("Hồ Sơ Mưa Đen", "ho-so-mua-den", author,
                "Một vụ án không có nạn nhân.", NovelStatus.ONGOING, 200,
                new BigDecimal("4.80"), 12, Set.of(mystery, socialRealism)));
    }

    @Test
    void publicCatalogSupportsSearchCategoryAndStatusFilters() throws Exception {
        mockMvc.perform(get("/api/public/catalog/novels")
                        .param("query", "mưa đen")
                        .param("category", "trinh-tham")
                        .param("status", "ongoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].slug").value("ho-so-mua-den"))
                .andExpect(jsonPath("$.content[0].authorName").value("Minh Dư"));
    }

    @Test
    void publicCatalogReturnsCategoriesAndNovelDetailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'trinh-tham')]").exists());

        mockMvc.perform(get("/api/public/catalog/novels/ho-so-mua-den"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hồ Sơ Mưa Đen"))
                .andExpect(jsonPath("$.categories.length()").value(2));
    }

    @Test
    void catalogRejectsInvalidStatusAndMissingNovel() throws Exception {
        mockMvc.perform(get("/api/public/catalog/novels").param("status", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_NOVEL_STATUS"));

        mockMvc.perform(get("/api/public/catalog/novels/khong-ton-tai"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOVEL_NOT_FOUND"));
    }
}
