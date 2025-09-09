package com.example.springboot_mysql_login_ci_sonar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "重置令牌不能為空")
    private String token;
    
    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "密碼長度至少 6 字元")
    private String newPassword;
}