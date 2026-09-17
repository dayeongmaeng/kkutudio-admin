package com.kkutudio.admin.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kkutudio.admin.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AuthControllerTest는 클래스 레벨 @Transactional로 각 테스트를 감싸 격리하는데,
 * 그 안에서는 AuthService의 트랜잭션이 REQUIRED로 "합류"하기 때문에 커밋 여부와 무관하게
 * 같은 트랜잭션 안에서는 자신이 쓴 값을 그대로 읽는다(read-your-own-writes).
 * 즉 요청 간에 실제로 커밋되는지는 검증하지 못한다. 이 테스트는 트랜잭션 경계 없이
 * AuthService를 직접, 별도의 트랜잭션으로 여러 번 호출해 "다음 요청에서도 실패 횟수가
 * 유지되는지"(=제대로 커밋되는지)를 검증한다.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        // admin_login_history가 admin_user를 참조하므로(ON DELETE CASCADE 없음) 자식부터 지운다.
        jdbcTemplate.update("DELETE FROM admin_login_history WHERE attempted_email = ?",
                "lockout-tester@kkutudio.com");
        adminUserRepository.findByEmail("lockout-tester@kkutudio.com").ifPresent(adminUserRepository::delete);
    }

    @Test
    void failedAttemptsPersistAcrossSeparateCallsAndEventuallyLock() {
        adminUserRepository.save(new AdminUser("lockout-tester@kkutudio.com",
                passwordEncoder.encode("correct-password"), "테스터"));

        for (int i = 0; i < SecurityConfig.MAX_FAILED_LOGIN_ATTEMPTS; i++) {
            assertThatThrownBy(() -> authService.login("lockout-tester@kkutudio.com", "wrong-password", "127.0.0.1",
                    "junit"))
                    .isInstanceOf(BadCredentialsException.class);
        }

        AdminUser reloaded = adminUserRepository.findByEmail("lockout-tester@kkutudio.com").orElseThrow();
        assertThat(reloaded.getFailedLoginCount()).isEqualTo(SecurityConfig.MAX_FAILED_LOGIN_ATTEMPTS);
        assertThat(reloaded.isCurrentlyLocked()).isTrue();

        // 잠긴 뒤에는 올바른 비밀번호를 넣어도 거부되어야 한다.
        assertThatThrownBy(() -> authService.login("lockout-tester@kkutudio.com", "correct-password", "127.0.0.1",
                "junit"))
                .isInstanceOf(org.springframework.security.authentication.LockedException.class);
    }
}
