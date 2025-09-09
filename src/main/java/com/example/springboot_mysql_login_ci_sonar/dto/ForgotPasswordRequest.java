package com.example.springboot_mysql_login_ci_sonar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "登入 ID 不能為空")
    private String loginId;
}