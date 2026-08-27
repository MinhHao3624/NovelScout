package com.minhhao.novelscout.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterWithOtpRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 30, message = "Tên đăng nhập từ 3 đến 30 ký tự")
        String username,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        String password,

        String displayName,

        @NotBlank(message = "Mã xác thực OTP không được để trống")
        @Size(min = 6, max = 6, message = "Mã xác thực OTP phải gồm 6 chữ số")
        String otpCode
) {}
