package com.minhhao.novelscout.crawler.wikisource;

import com.minhhao.novelscout.catalog.Novel;

public record PersistedWork(Novel novel, boolean created, int chapterCount) {}
