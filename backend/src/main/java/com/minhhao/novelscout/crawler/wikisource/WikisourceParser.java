package com.minhhao.novelscout.crawler.wikisource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WikisourceParser {
    private static final String BASE_URL = "https://vi.wikisource.org/wiki/";
    private static final Safelist CONTENT_SAFELIST = Safelist.simpleText()
            .addTags("p", "br", "blockquote", "h2", "h3", "ul", "ol", "li", "div", "span", "small");

    WikisourceWorkPreview parseWork(String sourcePage, MediaWikiResponse.MediaWikiParse page) {
        Document document = Jsoup.parse(page.text());
        String title = normalizeWorkTitle(textOr(document.selectFirst("#ws-title"), page.title()));
        String author = textOr(document.selectFirst("#ws-author"), "Khuyết danh");
        Element descriptionElement = document.selectFirst(".header_notes_content p, .header-notes-content p");
        String description = descriptionElement == null
                ? "Tác phẩm “" + title + "” của " + author + "."
                : descriptionElement.text().trim();

        Set<String> categories = new LinkedHashSet<>();
        if (page.categories() != null) {
            page.categories().stream().map(MediaWikiResponse.MediaWikiCategory::category).forEach(categories::add);
        }
        String license = resolveLicense(categories);

        LinkedHashMap<String, WikisourceChapterLink> chapters = new LinkedHashMap<>();
        String prefix = sourcePage + "/";
        Set<String> existingPages = page.links() == null ? Set.of() : page.links().stream()
                .filter(link -> Boolean.TRUE.equals(link.exists()))
                .map(MediaWikiResponse.MediaWikiLink::title)
                .collect(Collectors.toSet());
        for (Element link : document.select("a[title][href]")) {
            String linkedPage = link.attr("title").trim();
            if (!linkedPage.startsWith(prefix) || linkedPage.contains(":")) continue;
            if (page.links() != null && !page.links().isEmpty() && !existingPages.contains(linkedPage)) continue;
            String chapterTitle = link.text().trim();
            if (chapterTitle.isBlank()) chapterTitle = linkedPage.substring(prefix.length());
            chapters.putIfAbsent(linkedPage,
                    new WikisourceChapterLink(linkedPage, chapterTitle, canonicalUrl(linkedPage)));
        }
        if (chapters.isEmpty()) throw new WikisourceException("Tác phẩm không có mục lục chương: " + sourcePage);

        return new WikisourceWorkPreview(sourcePage, canonicalUrl(sourcePage), title, author, description,
                license, categories, List.copyOf(chapters.values()));
    }

    WikisourceChapterData parseChapter(WikisourceChapterLink chapter, MediaWikiResponse.MediaWikiParse page) {
        Document document = Jsoup.parse(page.text());
        String title = textOr(document.selectFirst("#header_section_text"), chapter.title());
        Element content = document.selectFirst(".prose");
        if (content == null) content = document.selectFirst(".mw-parser-output");
        if (content == null) throw new WikisourceException("Không tìm thấy nội dung chương: " + chapter.page());

        content.select("#headerContainer, .ws-noexport, .noprint, .mw-editsection, .metadata, " +
                ".licenseContainer, .navbox, script, style, noscript").remove();
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        String sanitized = Jsoup.clean(content.html(), "", CONTENT_SAFELIST, outputSettings).trim();
        if (Jsoup.parse(sanitized).text().length() < 200) {
            throw new WikisourceException("Nội dung chương quá ngắn hoặc không hợp lệ: " + chapter.page());
        }
        return new WikisourceChapterData(chapter.page(), title, chapter.sourceUrl(), sanitized);
    }

    private String resolveLicense(Set<String> categories) {
        boolean publicDomain = categories.stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(value -> value.startsWith("PVCC") || value.contains("PHẠM_VI_CÔNG_CỘNG"));
        if (publicDomain) return "PUBLIC_DOMAIN";
        boolean creativeCommons = categories.stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(value -> value.contains("CC-BY") || value.contains("CREATIVE_COMMONS"));
        if (creativeCommons) return "CC_BY_SA";
        throw new WikisourceException("Không xác nhận được giấy phép mở từ metadata Wikisource");
    }

    static String canonicalUrl(String page) {
        String encoded = Arrays.stream(page.replace(' ', '_').split("/", -1))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .reduce((left, right) -> left + "/" + right)
                .orElseThrow();
        return BASE_URL + encoded;
    }

    private String textOr(Element element, String fallback) {
        return element == null || element.text().isBlank() ? fallback : element.text().trim();
    }

    private String normalizeWorkTitle(String title) {
        return title.replaceFirst("\\s+[—–-]\\s+Mục lục$", "").trim();
    }
}
