package com.minhhao.novelscout.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Nội dung bình luận không được để trống")
        @Size(max = 2000, message = "Nội dung bình luận tối đa 2000 ký tự")
        String content,

        Long parentId,

        Long chapterId
) {}
