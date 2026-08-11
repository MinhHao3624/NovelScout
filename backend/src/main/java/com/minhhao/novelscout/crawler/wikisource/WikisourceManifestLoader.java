package com.minhhao.novelscout.crawler.wikisource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class WikisourceManifestLoader {
    private final ObjectMapper objectMapper;
    private final Resource manifestResource;

    public WikisourceManifestLoader(
                                    @Value("${app.crawler.wikisource.manifest:classpath:crawler/wikisource-vi-manifest.json}")
                                    Resource manifestResource) {
        this.objectMapper = new ObjectMapper();
        this.manifestResource = manifestResource;
    }

    public List<String> load() {
        try (var input = manifestResource.getInputStream()) {
            WikisourceManifest manifest = objectMapper.readValue(input, WikisourceManifest.class);
            return manifest.works().stream().map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
        } catch (IOException exception) {
            throw new WikisourceException("Không đọc được manifest Wikisource", exception);
        }
    }

    private record WikisourceManifest(List<String> works) {}
}
