package com.minhhao.novelscout.admin.dto;

import com.minhhao.novelscout.catalog.NovelStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record AdminNovelRequest(
        @NotBlank(message = "Tên truyện không được để trống")
        String title,

        String authorName,
        String description,
        String coverUrl,
        NovelStatus status,
        Set<Long> categoryIds
) {}

