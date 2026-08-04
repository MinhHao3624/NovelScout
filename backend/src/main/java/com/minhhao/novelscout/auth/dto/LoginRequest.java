package com.minhhao.novelscout.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email hoặc tên đăng nhập không được để trống")
        @Size(max = 255, message = "Thông tin đăng nhập không hợp lệ") String login,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 72, message = "Mật khẩu không hợp lệ") String password
) {}
