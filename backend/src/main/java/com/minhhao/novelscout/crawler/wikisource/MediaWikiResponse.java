package com.minhhao.novelscout.crawler.wikisource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record MediaWikiResponse(MediaWikiParse parse, MediaWikiError error) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record MediaWikiParse(String title, String displaytitle, String text, List<MediaWikiLink> links,
                          List<String> images, List<MediaWikiCategory> categories) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MediaWikiLink(String title, String url, Boolean exists) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MediaWikiCategory(String category, String sortkey) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MediaWikiError(String code, String info) {}
}
