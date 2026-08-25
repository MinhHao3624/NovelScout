package com.minhhao.novelscout.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminChapterRequest(
        @NotNull(message = "Số thứ tự chương không được để trống")
        Integer chapterNumber,

        @NotBlank(message = "Tên chương không được để trống")
        String title,

        @NotBlank(message = "Nội dung chương không được để trống")
        String content
) {}
