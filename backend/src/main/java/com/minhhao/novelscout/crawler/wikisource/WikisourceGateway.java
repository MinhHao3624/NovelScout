package com.minhhao.novelscout.crawler.wikisource;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WikisourceGateway {
    private final WikisourceClient client;
    private final WikisourceParser parser;

    public WikisourceGateway(WikisourceClient client, WikisourceParser parser) {
        this.client = client;
        this.parser = parser;
    }

    public WikisourceWorkData fetchWork(String sourcePage) {
        WikisourceWorkPreview preview = parser.parseWork(sourcePage, client.fetchPage(sourcePage));
        List<WikisourceChapterData> chapters = new ArrayList<>();
        for (WikisourceChapterLink chapter : preview.chapters()) {
            chapters.add(parser.parseChapter(chapter, client.fetchPage(chapter.page())));
        }
        return new WikisourceWorkData(preview.sourcePage(), preview.sourceUrl(), preview.title(), preview.author(),
                preview.description(), preview.license(), preview.sourceCategories(), List.copyOf(chapters));
    }
}
