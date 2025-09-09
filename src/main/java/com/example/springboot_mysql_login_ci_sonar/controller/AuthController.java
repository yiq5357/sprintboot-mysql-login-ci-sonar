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
     * TEST
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
     * 健康檢查 API
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("認證服務正常運行"));
    }
}


