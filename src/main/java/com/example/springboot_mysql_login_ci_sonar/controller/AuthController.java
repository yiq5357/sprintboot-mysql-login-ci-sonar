package com.example.springboot_mysql_login_ci_sonar.controller;

import com.example.springboot_mysql_login_ci_sonar.dto.*;
import com.example.springboot_mysql_login_ci_sonar.entity.User;
import com.example.springboot_mysql_login_ci_sonar.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證相關 API Controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    /**
     * 用戶註冊 API
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("收到註冊請求: username={}, loginId={}", request.getUsername(), request.getLoginId());

        try {
            User user = userService.signup(
                request.getUsername(),
                request.getLoginId(),
                request.getPassword()
            );

            UserResponse userResponse = UserResponse.from(user);
            ApiResponse<UserResponse> response = ApiResponse.success("註冊成功", userResponse);

            log.info("註冊成功: userId={}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("註冊失敗: {}", e.getMessage());
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("註冊過程中發生錯誤", e);
            ApiResponse<UserResponse> response = ApiResponse.error("註冊失敗，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 用戶登入 API
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登入請求: loginId={}", request.getLoginId());

        try {
            var userOpt = userService.login(request.getLoginId(), request.getPassword());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserResponse userResponse = UserResponse.from(user);
                ApiResponse<UserResponse> response = ApiResponse.success("登入成功", userResponse);

                log.info("登入成功: userId={}, username={}", user.getId(), user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("登入失敗: 無效的登入憑證 loginId={}", request.getLoginId());
                ApiResponse<UserResponse> response = ApiResponse.error("登入失敗，請檢查登入 ID 和密碼");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            log.error("登入過程中發生錯誤", e);
            ApiResponse<UserResponse> response = ApiResponse.error("登入失敗，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 申請密碼重置 API
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("收到密碼重置申請: loginId={}", request.getLoginId());
        
        try {
            boolean success = userService.requestPasswordReset(request.getLoginId());
            
            if (success) {
                // 為了安全考量，不論用戶是否存在都返回成功訊息
                ApiResponse<String> response = ApiResponse.success(
                    "如果該登入 ID 存在，重置驗證碼已發送。請檢查您的簡訊或郵件。"
                );
                return ResponseEntity.ok(response);
            } else {
                // 同樣返回成功訊息，避免洩露用戶是否存在
                ApiResponse<String> response = ApiResponse.success(
                    "如果該登入 ID 存在，重置驗證碼已發送。請檢查您的簡訊或郵件。"
                );
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("密碼重置申請過程中發生錯誤", e);
            ApiResponse<String> response = ApiResponse.error("申請失敗，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 重置密碼 API
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("收到密碼重置請求: token={}", request.getToken());
        
        try {
            boolean success = userService.resetPassword(request.getToken(), request.getNewPassword());
            
            if (success) {
                ApiResponse<String> response = ApiResponse.success("密碼重置成功，請使用新密碼登入");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = ApiResponse.error("重置失敗，令牌無效或已過期");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("密碼重置過程中發生錯誤", e);
            ApiResponse<String> response = ApiResponse.error("重置失敗，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }    
    
    /**
     * 健康檢查 API
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("認證服務正常運行"));
    }
}


