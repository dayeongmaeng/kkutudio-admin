package com.kkutudio.admin.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "admin_login_history")
public class AdminLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "attempted_email", nullable = false)
    private String attemptedEmail;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason")
    private String failureReason;

    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AdminLoginHistory() {
    }

    public static AdminLoginHistory success(Long adminUserId, String attemptedEmail, String ip, String userAgent) {
        AdminLoginHistory history = new AdminLoginHistory();
        history.adminUserId = adminUserId;
        history.attemptedEmail = attemptedEmail;
        history.success = true;
        history.ip = ip;
        history.userAgent = userAgent;
        return history;
    }

    public static AdminLoginHistory failure(Long adminUserId, String attemptedEmail, String failureReason, String ip,
            String userAgent) {
        AdminLoginHistory history = new AdminLoginHistory();
        history.adminUserId = adminUserId;
        history.attemptedEmail = attemptedEmail;
        history.success = false;
        history.failureReason = failureReason;
        history.ip = ip;
        history.userAgent = userAgent;
        return history;
    }

    public Long getId() {
        return id;
    }
}
