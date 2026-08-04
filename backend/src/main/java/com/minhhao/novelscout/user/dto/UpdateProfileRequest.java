package com.minhhao.novelscout.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(max = 150, message = "Tên hiển thị không được quá 150 ký tự") String displayName,
        @Size(max = 500, message = "Địa chỉ ảnh không được quá 500 ký tự")
        @Pattern(regexp = "^(|https?://.+)$", message = "Ảnh đại diện phải là một địa chỉ HTTP hợp lệ") String avatarUrl
) {}
