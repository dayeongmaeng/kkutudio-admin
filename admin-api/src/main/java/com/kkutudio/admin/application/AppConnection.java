package com.kkutudio.admin.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 앱 admin API 연동 정보. credentialEnvKey는 비밀값 자체가 아니라
 * 비밀값이 들어있는 환경변수의 "이름"이다 (실제 값은 절대 DB에 저장하지 않는다).
 */
@Entity
@Table(name = "app_connection")
public class AppConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "app_id", nullable = false, unique = true)
    private ManagedApp app;

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AppConnectionAuthType authType = AppConnectionAuthType.API_KEY;

    @Column(name = "credential_env_key", nullable = false)
    private String credentialEnvKey;

    @Column(name = "timeout_ms", nullable = false)
    private int timeoutMs = 5000;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_health_status")
    private AppHealthStatus lastHealthStatus;

    @Column(name = "last_health_checked_at")
    private Instant lastHealthCheckedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppConnection() {
    }

    public Long getId() {
        return id;
    }

    public ManagedApp getApp() {
        return app;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public AppConnectionAuthType getAuthType() {
        return authType;
    }

    public String getCredentialEnvKey() {
        return credentialEnvKey;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void updateHealth(AppHealthStatus status) {
        this.lastHealthStatus = status;
        this.lastHealthCheckedAt = Instant.now();
    }
}
