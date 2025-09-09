package com.example.springboot_mysql_login_ci_sonar.repository;

import com.example.springboot_mysql_login_ci_sonar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User 資料存取層
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根據登入 ID 查找用戶
     * @param loginId 登入 ID
     * @return Optional<User>
     */
    Optional<User> findByLoginId(String loginId);

    /**
     * 根據用戶名稱查找用戶
     * @param username 用戶名稱
     * @return Optional<User>
     */
    Optional<User> findByUsername(String username);

    /**
     * 根據重置令牌查找用戶
     */
    Optional<User> findByResetToken(String resetToken);    
    
    /**
     * 檢查登入 ID 是否已存在
     * @param loginId 登入 ID
     * @return boolean
     */
    boolean existsByLoginId(String loginId);

    /**
     * 檢查用戶名稱是否已存在
     * @param username 用戶名稱
     * @return boolean
     */
    boolean existsByUsername(String username);

    /**
     * 根據登入 ID 和密碼查找用戶（用於登入驗證）
     * @param loginId 登入 ID
     * @param password 密碼
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE u.loginId = :loginId AND u.password = :password AND u.enabled = true")
    Optional<User> findByLoginIdAndPassword(@Param("loginId") String loginId, @Param("password") String password);
    
    @Modifying
    @Query("UPDATE User u SET u.resetToken = null, u.resetTokenExpiry = null WHERE u.resetTokenExpiry < :now")
    void clearExpiredResetTokens(@Param("now") LocalDateTime now);    
    
}


