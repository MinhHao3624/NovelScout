package com.minhhao.novelscout.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "Mã xác thực Google Credential Token không được để trống")
        String credential
) {}
