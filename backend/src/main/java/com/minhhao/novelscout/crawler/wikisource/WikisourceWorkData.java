package com.minhhao.novelscout.crawler.wikisource;

import java.util.List;
import java.util.Set;

public record WikisourceWorkData(String sourcePage, String sourceUrl, String title, String author,
                                 String description, String license, Set<String> sourceCategories,
                                 List<WikisourceChapterData> chapters) {}
