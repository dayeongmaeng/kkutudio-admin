package com.kkutudio.admin.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLoginHistoryRepository extends JpaRepository<AdminLoginHistory, Long> {
}
