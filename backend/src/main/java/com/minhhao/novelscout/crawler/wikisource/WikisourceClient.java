package com.minhhao.novelscout.crawler.wikisource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class WikisourceClient {
    private final RestClient restClient;
    private final long delayMs;
    private final int maxAttempts;
    private long lastRequestAt;

    public WikisourceClient(@Value("${app.crawler.wikisource.base-url:https://vi.wikisource.org}") String baseUrl,
                            @Value("${app.crawler.wikisource.user-agent:NovelScoutBot/0.1 (academic project)}") String userAgent,
                            @Value("${app.crawler.default-delay-ms:1000}") long delayMs,
                            @Value("${app.crawler.wikisource.max-attempts:3}") int maxAttempts) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("User-Agent", userAgent).build();
        this.delayMs = Math.max(250, delayMs);
        this.maxAttempts = Math.clamp(maxAttempts, 1, 5);
    }

    public MediaWikiResponse.MediaWikiParse fetchPage(String page) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            waitForRateLimit();
            try {
                MediaWikiResponse response = restClient.get()
                        .uri(uri -> uri.path("/w/api.php")
                                .queryParam("action", "parse")
                                .queryParam("page", page)
                                .queryParam("prop", "text|links|images|categories|displaytitle")
                                .queryParam("format", "json")
                                .queryParam("formatversion", "2")
                                .build())
                        .retrieve()
                        .body(MediaWikiResponse.class);
                if (response == null) throw new WikisourceException("Wikisource trả về phản hồi rỗng");
                if (response.error() != null) {
                    throw new WikisourceException(page + " - " + response.error().code() + ": " + response.error().info());
                }
                if (response.parse() == null) throw new WikisourceException("Không đọc được trang: " + page);
                return response.parse();
            } catch (ResourceAccessException | RestClientResponseException exception) {
                lastError = exception;
                if (attempt < maxAttempts) waitBeforeRetry(attempt);
            }
        }
        throw new WikisourceException("Không thể tải trang sau " + maxAttempts + " lần: " + page, lastError);
    }

    private synchronized void waitForRateLimit() {
        long waitMs = delayMs - (System.currentTimeMillis() - lastRequestAt);
        if (waitMs > 0) sleep(waitMs);
        lastRequestAt = System.currentTimeMillis();
    }

    private void waitBeforeRetry(int attempt) {
        sleep(Math.min(8_000L, 1_000L << (attempt - 1)));
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WikisourceException("Tiến trình import đã bị dừng", exception);
        }
    }
}
