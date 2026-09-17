package com.kkutudio.admin.member;

import com.kkutudio.admin.application.AppConnection;
import com.kkutudio.admin.application.AppConnectionRepository;
import com.kkutudio.admin.audit.AuditLogService;
import com.kkutudio.admin.auth.AdminUserPrincipal;
import com.kkutudio.admin.integration.AppAdminClient;
import com.kkutudio.admin.integration.AppAdminClientFactory;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

/** Admin DB에는 회원 데이터를 저장하지 않고, 매 요청마다 앱 admin API를 실시간으로 호출한다. */
@Service
public class MemberService {

    private static final String TARGET_TYPE = "MEMBER";

    private final AppConnectionRepository appConnectionRepository;
    private final AppAdminClientFactory clientFactory;
    private final AuditLogService auditLogService;

    public MemberService(AppConnectionRepository appConnectionRepository, AppAdminClientFactory clientFactory,
            AuditLogService auditLogService) {
        this.appConnectionRepository = appConnectionRepository;
        this.clientFactory = clientFactory;
        this.auditLogService = auditLogService;
    }

    public AppAdminClient.MemberPageResponse getMembers(String appCode, String keyword, int page, int size,
            AdminUserPrincipal admin, String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "MEMBER_VIEW", null, null, ip, userAgent,
                () -> client(connection).getMembers(keyword, page, size));
    }

    public AppAdminClient.MemberDetailResponse getMember(String appCode, String memberId, AdminUserPrincipal admin,
            String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "MEMBER_VIEW", memberId, null, ip, userAgent,
                () -> client(connection).getMember(memberId));
    }

    public void suspendMember(String appCode, String memberId, String reason, AdminUserPrincipal admin, String ip,
            String userAgent) {
        AppConnection connection = requireConnection(appCode);
        withAudit(connection, admin, "MEMBER_SUSPEND", memberId, reason, ip, userAgent, () -> {
            client(connection).suspendMember(memberId, new AppAdminClient.SuspendMemberRequest(reason));
            return null;
        });
    }

    public void forceLogoutMember(String appCode, String memberId, AdminUserPrincipal admin, String ip,
            String userAgent) {
        AppConnection connection = requireConnection(appCode);
        withAudit(connection, admin, "MEMBER_FORCE_LOGOUT", memberId, null, ip, userAgent, () -> {
            client(connection).forceLogoutMember(memberId);
            return null;
        });
    }

    private <T> T withAudit(AppConnection connection, AdminUserPrincipal admin, String action, String targetId,
            String reason, String ip, String userAgent, java.util.function.Supplier<T> call) {
        try {
            T result = call.get();
            auditLogService.recordSuccess(admin.getId(), connection.getApp().getId(), action, TARGET_TYPE, targetId,
                    reason, result, ip, userAgent);
            return result;
        } catch (RuntimeException e) {
            auditLogService.recordFailure(admin.getId(), connection.getApp().getId(), action, TARGET_TYPE, targetId,
                    reason, e.getMessage(), ip, userAgent);
            throw e;
        }
    }

    private AppAdminClient client(AppConnection connection) {
        return clientFactory.create(connection);
    }

    private AppConnection requireConnection(String appCode) {
        return appConnectionRepository.findByApp_AppCode(appCode)
                .orElseThrow(() -> new NoSuchElementException("No connection configured for app: " + appCode));
    }
}
