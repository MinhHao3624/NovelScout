package com.minhhao.novelscout.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng")
        @Size(max = 255, message = "Email không được quá 255 ký tự") String email,
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 30, message = "Tên đăng nhập phải có từ 3 đến 30 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập chỉ gồm chữ, số và dấu gạch dưới") String username,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải có từ 8 đến 72 ký tự") String password,
        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(max = 150, message = "Tên hiển thị không được quá 150 ký tự") String displayName
) {}
