package com.minhhao.novelscout.crawler.wikisource;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WikisourceParserTests {
    private final WikisourceParser parser = new WikisourceParser();

    @Test
    void parsesMetadataLicenseAndChapterOrder() {
        String html = """
                <div class="mw-parser-output">
                  <div id="headerContainer">
                    <span id="ws-title">Tắt đèn</span><span id="ws-author">Ngô Tất Tố</span>
                    <div class="header_notes_content"><p>Một tác phẩm văn học hiện thực.</p></div>
                  </div>
                  <h2>Mục lục</h2>
                  <ul><li><a href="/wiki/Tat/I" title="Tắt đèn/I">I</a></li>
                      <li><a href="/wiki/Tat/II" title="Tắt đèn/II">II</a></li></ul>
                </div>
                """;
        var response = new MediaWikiResponse.MediaWikiParse("Tắt đèn", "Tắt đèn", html, List.of(
                new MediaWikiResponse.MediaWikiLink("Tắt đèn/I", null, true),
                new MediaWikiResponse.MediaWikiLink("Tắt đèn/II", null, true)),
                List.of(), List.of(new MediaWikiResponse.MediaWikiCategory("PVCC-Việt_Nam", ""),
                        new MediaWikiResponse.MediaWikiCategory("Tiểu_thuyết", "")));

        WikisourceWorkPreview preview = parser.parseWork("Tắt đèn", response);

        assertThat(preview.title()).isEqualTo("Tắt đèn");
        assertThat(preview.author()).isEqualTo("Ngô Tất Tố");
        assertThat(preview.license()).isEqualTo("PUBLIC_DOMAIN");
        assertThat(preview.chapters()).extracting(WikisourceChapterLink::title).containsExactly("I", "II");
    }

    @Test
    void sanitizesChapterAndRejectsUnknownLicense() {
        String chapterHtml = """
                <div class="mw-parser-output"><div id="headerContainer">Điều hướng</div>
                  <div class="prose"><script>alert('x')</script><p>Đây là nội dung chương hợp lệ.
                  Nội dung được lặp lại đủ dài để vượt qua kiểm tra tối thiểu và bảo đảm parser không
                  lưu các thành phần điều hướng hoặc mã script không an toàn vào cơ sở dữ liệu.
                  Đoạn văn tiếp tục với nhiều câu chữ tiếng Việt nhằm mô phỏng một chương truyện thật.</p></div>
                </div>
                """;
        var chapterResponse = new MediaWikiResponse.MediaWikiParse("Tắt đèn/I", "I", chapterHtml,
                List.of(), List.of(), List.of());

        WikisourceChapterData data = parser.parseChapter(
                new WikisourceChapterLink("Tắt đèn/I", "I", "https://example/I"), chapterResponse);

        assertThat(data.sanitizedHtml()).contains("<p>").doesNotContain("<script", "headerContainer");

        var unlicensed = new MediaWikiResponse.MediaWikiParse("Test", "Test",
                "<span id='ws-title'>Test</span><a title='Test/I' href='/I'>I</a>",
                List.of(), List.of(), List.of());
        assertThatThrownBy(() -> parser.parseWork("Test", unlicensed))
                .isInstanceOf(WikisourceException.class)
                .hasMessageContaining("giấy phép");
    }

    @Test
    void curatedTaxonomyCoversEveryManifestWorkAndOnlyKnownCategories() {
        WikisourceManifestLoader manifest = new WikisourceManifestLoader(
                new ClassPathResource("crawler/wikisource-vi-manifest.json"));
        WikisourceTaxonomyRegistry taxonomy = new WikisourceTaxonomyRegistry(
                new ClassPathResource("crawler/wikisource-vi-taxonomy.json"));
        Set<String> knownCategories = Set.of(
                "van-hoc-viet-nam", "van-hoc-nuoc-ngoai", "hien-thuc-xa-hoi", "tam-ly", "tinh-cam",
                "gia-dinh", "dao-ly-nhan-qua", "lich-su", "chien-tranh", "phieu-luu", "trinh-tham",
                "toi-pham", "bao-thu", "bi-kich", "hai-huoc-trao-phung", "than-phan-phu-nu",
                "nghia-hiep", "phong-tuc", "nong-thon");

        assertThat(taxonomy.sourcePages()).containsExactlyInAnyOrderElementsOf(manifest.load());
        assertThat(taxonomy.sourcePages())
                .allSatisfy(page -> assertThat(taxonomy.categorySlugsFor(page)).isSubsetOf(knownCategories));
    }
}
