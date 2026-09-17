package com.kkutudio.admin.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * admin_user 테이블이 비어 있고 ADMIN_BOOTSTRAP_EMAIL / ADMIN_BOOTSTRAP_PASSWORD 환경변수가
 * 설정되어 있으면 최초 관리자 계정을 생성한다. 비밀번호를 마이그레이션이나 코드에 커밋하지 않기 위함.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminBootstrapRunner(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder,
            Environment environment) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }

        String email = environment.getProperty("ADMIN_BOOTSTRAP_EMAIL");
        String password = environment.getProperty("ADMIN_BOOTSTRAP_PASSWORD");
        String name = environment.getProperty("ADMIN_BOOTSTRAP_NAME", "관리자");

        if (email == null || password == null) {
            log.warn("admin_user 테이블이 비어 있지만 ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD가 설정되지 않아 "
                    + "초기 관리자 계정을 생성하지 않았습니다.");
            return;
        }

        AdminUser adminUser = new AdminUser(email, passwordEncoder.encode(password), name);
        adminUserRepository.save(adminUser);
        log.info("초기 관리자 계정을 생성했습니다: {}", email);
    }
}
