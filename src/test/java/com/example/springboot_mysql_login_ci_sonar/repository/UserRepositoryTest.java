package com.example.springboot_mysql_login_ci_sonar.repository;

import com.example.springboot_mysql_login_ci_sonar.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository 整合測試
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("測試用戶");
        testUser.setLoginId("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setEnabled(true);
    }

    @Test
    void testSaveAndFindById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("測試用戶", foundUser.get().getUsername());
        assertEquals("testuser", foundUser.get().getLoginId());
        assertEquals("encodedPassword", foundUser.get().getPassword());
        assertTrue(foundUser.get().getEnabled());
    }

    @Test
    void testFindByLoginId() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByLoginId("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("測試用戶", foundUser.get().getUsername());
        assertEquals("testuser", foundUser.get().getLoginId());
    }

    @Test
    void testFindByLoginId_NotFound() {
        // When
        Optional<User> foundUser = userRepository.findByLoginId("nonexistent");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByUsername("測試用戶");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("測試用戶", foundUser.get().getUsername());
        assertEquals("testuser", foundUser.get().getLoginId());
    }

    @Test
    void testFindByUsername_NotFound() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("不存在的用戶");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testExistsByLoginId() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByLoginId("testuser");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByLoginId_NotExists() {
        // When
        boolean exists = userRepository.existsByLoginId("nonexistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void testExistsByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByUsername("測試用戶");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_NotExists() {
        // When
        boolean exists = userRepository.existsByUsername("不存在的用戶");

        // Then
        assertFalse(exists);
    }

    @Test
    void testFindByLoginIdAndPassword() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByLoginIdAndPassword("testuser", "encodedPassword");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("測試用戶", foundUser.get().getUsername());
        assertEquals("testuser", foundUser.get().getLoginId());
    }

    @Test
    void testFindByLoginIdAndPassword_WrongPassword() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByLoginIdAndPassword("testuser", "wrongPassword");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByLoginIdAndPassword_DisabledUser() {
        // Given
        testUser.setEnabled(false);
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByLoginIdAndPassword("testuser", "encodedPassword");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testUniqueConstraints() {
        // Given
        entityManager.persistAndFlush(testUser);

        // 測試登入 ID 唯一約束
        User duplicateLoginIdUser = new User();
        duplicateLoginIdUser.setUsername("另一個用戶");
        duplicateLoginIdUser.setLoginId("testuser"); // 重複的登入 ID
        duplicateLoginIdUser.setPassword("password");
        duplicateLoginIdUser.setEnabled(true);

        // 這應該會拋出異常，因為登入 ID 有唯一約束
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateLoginIdUser);
        });
    }
    
    
}

