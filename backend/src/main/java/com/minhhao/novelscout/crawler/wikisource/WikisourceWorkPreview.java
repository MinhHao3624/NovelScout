package com.minhhao.novelscout.crawler.wikisource;

import java.util.List;
import java.util.Set;

record WikisourceWorkPreview(String sourcePage, String sourceUrl, String title, String author,
                             String description, String license, Set<String> sourceCategories,
                             List<WikisourceChapterLink> chapters) {}
