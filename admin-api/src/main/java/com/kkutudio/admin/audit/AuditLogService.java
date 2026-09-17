package com.kkutudio.admin.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void recordSuccess(Long adminUserId, Long appId, String action, String targetType, String targetId,
            String reason, Object afterValue, String ip, String userAgent) {
        record(adminUserId, appId, action, targetType, targetId, reason, null, afterValue, "SUCCESS", null, ip,
                userAgent);
    }

    public void recordFailure(Long adminUserId, Long appId, String action, String targetType, String targetId,
            String reason, String errorMessage, String ip, String userAgent) {
        record(adminUserId, appId, action, targetType, targetId, reason, null, null, "FAILURE", errorMessage, ip,
                userAgent);
    }

    private void record(Long adminUserId, Long appId, String action, String targetType, String targetId,
            String reason, Object beforeValue, Object afterValue, String result, String errorMessage, String ip,
            String userAgent) {
        AuditLog auditLog = new AuditLog(adminUserId, appId, action, targetType, targetId, reason,
                toJson(beforeValue), toJson(afterValue), result, errorMessage, ip, userAgent);
        auditLogRepository.save(auditLog);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            log.warn("Failed to serialize audit log value", e);
            return null;
        }
    }
}
