package com.minhhao.novelscout.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTI_DASH = Pattern.compile("-+");

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "no-title";
        }
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = MULTI_DASH.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-|-$", "");
        return slug.isEmpty() ? "novel" : slug;
    }
}
