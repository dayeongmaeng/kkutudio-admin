package com.kkutudio.admin.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final AdminLoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminUserRepository adminUserRepository, AdminLoginHistoryRepository loginHistoryRepository,
            PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 실패 시 던지는 인증 예외들은 RuntimeException이라 기본 규칙대로면 트랜잭션이 롤백되어
    // 실패 이력 기록과 잠금 카운트 증가까지 함께 사라진다. noRollbackFor로 커밋을 강제한다.
    @Transactional(noRollbackFor = { BadCredentialsException.class, LockedException.class,
            DisabledException.class })
    public AdminUser login(String email, String rawPassword, String ip, String userAgent) {
        AdminUser adminUser = adminUserRepository.findByEmail(email).orElse(null);
        if (adminUser == null) {
            recordFailure(null, email, "BAD_CREDENTIALS", ip, userAgent);
            throw new BadCredentialsException("Invalid email or password");
        }
        if (adminUser.getStatus() == AdminUserStatus.DISABLED) {
            recordFailure(adminUser.getId(), email, "DISABLED", ip, userAgent);
            throw new DisabledException("Account disabled");
        }
        if (adminUser.isCurrentlyLocked()) {
            recordFailure(adminUser.getId(), email, "LOCKED", ip, userAgent);
            throw new LockedException("Account locked");
        }
        if (!passwordEncoder.matches(rawPassword, adminUser.getPasswordHash())) {
            adminUser.registerFailedLogin(SecurityConfig.MAX_FAILED_LOGIN_ATTEMPTS, SecurityConfig.LOCK_DURATION);
            recordFailure(adminUser.getId(), email, "BAD_CREDENTIALS", ip, userAgent);
            throw new BadCredentialsException("Invalid email or password");
        }
        adminUser.registerSuccessfulLogin(ip);
        loginHistoryRepository.save(AdminLoginHistory.success(adminUser.getId(), email, ip, userAgent));
        return adminUser;
    }

    private void recordFailure(Long adminUserId, String email, String reason, String ip, String userAgent) {
        loginHistoryRepository.save(AdminLoginHistory.failure(adminUserId, email, reason, ip, userAgent));
    }
}
