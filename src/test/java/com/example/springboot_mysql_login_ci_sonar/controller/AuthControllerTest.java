package com.example.springboot_mysql_login_ci_sonar.controller;

import com.example.springboot_mysql_login_ci_sonar.dto.*;
import com.example.springboot_mysql_login_ci_sonar.entity.User;
import com.example.springboot_mysql_login_ci_sonar.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * AuthController 單元測試
 */
@WebMvcTest(AuthController.class)
@Import(com.example.springboot_mysql_login_ci_sonar.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    
    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("測試用戶");
        testUser.setLoginId("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setEnabled(true);

        signupRequest = new SignupRequest();
        signupRequest.setUsername("測試用戶");
        signupRequest.setLoginId("testuser");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setLoginId("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testSignup_Success() throws Exception {
        // Given
        when(userService.signup(anyString(), anyString(), anyString())).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("註冊成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("測試用戶"))
                .andExpect(jsonPath("$.data.loginId").value("testuser"));
    }

    @Test
    void testSignup_UsernameAlreadyExists() throws Exception {
        // Given
        when(userService.signup(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("用戶名稱已存在"));

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用戶名稱已存在"));
    }

    @Test
    void testSignup_LoginIdAlreadyExists() throws Exception {
        // Given
        when(userService.signup(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("登入 ID 已存在"));

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("登入 ID 已存在"));
    }

    @Test
    void testSignup_InvalidRequest() throws Exception {
        // Given
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername(""); // 空的用戶名稱
        invalidRequest.setLoginId("ab"); // 太短的登入 ID
        invalidRequest.setPassword("123"); // 太短的密碼

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        when(userService.login(anyString(), anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登入成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("測試用戶"))
                .andExpect(jsonPath("$.data.loginId").value("testuser"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("登入失敗，請檢查登入 ID 和密碼"));
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setLoginId(""); // 空的登入 ID
        invalidRequest.setPassword(""); // 空的密碼

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("認證服務正常運行"));
    }
    
    @Test
    void testForgotPassword_Success() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setLoginId("testuser");
        when(userService.requestPasswordReset(anyString())).thenReturn(true);
        
        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("如果該登入 ID 存在，重置驗證碼已發送。請檢查您的簡訊或郵件。"));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("123456");
        request.setNewPassword("newpassword123");
        when(userService.resetPassword(anyString(), anyString())).thenReturn(true);
        
        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密碼重置成功，請使用新密碼登入"));
    }    
}