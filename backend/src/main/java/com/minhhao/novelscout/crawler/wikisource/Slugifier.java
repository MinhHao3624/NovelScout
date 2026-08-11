package com.minhhao.novelscout.crawler.wikisource;

import java.text.Normalizer;
import java.util.Locale;

final class Slugifier {
    private Slugifier() {}

    static String slugify(String value) {
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.ROOT)
                        .replace('đ', 'd'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "tac-pham" : normalized;
    }
}
