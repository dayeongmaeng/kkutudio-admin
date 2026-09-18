package com.kkutudio.admin.statistics;

import com.kkutudio.admin.application.AppConnection;
import com.kkutudio.admin.application.AppConnectionRepository;
import com.kkutudio.admin.audit.AuditLogService;
import com.kkutudio.admin.auth.AdminUserPrincipal;
import com.kkutudio.admin.integration.AppAdminClient;
import com.kkutudio.admin.integration.AppAdminClientFactory;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

/** Admin DB에는 통계 데이터를 저장하지 않고, 매 요청마다 앱 admin API를 실시간으로 호출한다. */
@Service
public class StatisticsService {

    private static final String TARGET_TYPE = "STATISTICS";
    private static final String ACTION = "STATISTICS_VIEW";

    private final AppConnectionRepository appConnectionRepository;
    private final AppAdminClientFactory clientFactory;
    private final AuditLogService auditLogService;

    public StatisticsService(AppConnectionRepository appConnectionRepository, AppAdminClientFactory clientFactory,
            AuditLogService auditLogService) {
        this.appConnectionRepository = appConnectionRepository;
        this.clientFactory = clientFactory;
        this.auditLogService = auditLogService;
    }

    public AppAdminClient.DashboardOverviewResponse getOverview(String appCode, String from, String to, String unit,
            AdminUserPrincipal admin, String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "overview", ip, userAgent,
                () -> client(connection).getDashboardOverview(from, to, unit));
    }

    public AppAdminClient.DashboardMembersResponse getMembers(String appCode, String from, String to, String unit,
            AdminUserPrincipal admin, String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "members", ip, userAgent,
                () -> client(connection).getDashboardMembers(from, to, unit));
    }

    public AppAdminClient.DashboardPetsResponse getPets(String appCode, String from, String to, String unit,
            AdminUserPrincipal admin, String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "pets", ip, userAgent,
                () -> client(connection).getDashboardPets(from, to, unit));
    }

    public AppAdminClient.DashboardRecordsResponse getRecords(String appCode, String from, String to, String unit,
            AdminUserPrincipal admin, String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "records", ip, userAgent,
                () -> client(connection).getDashboardRecords(from, to, unit));
    }

    public AppAdminClient.DashboardConversionResponse getConversion(String appCode, String from, String to,
            String unit, AdminUserPrincipal admin, String ip, String userAgent) {
        AppConnection connection = requireConnection(appCode);
        return withAudit(connection, admin, "conversion", ip, userAgent,
                () -> client(connection).getDashboardConversion(from, to, unit));
    }

    private <T> T withAudit(AppConnection connection, AdminUserPrincipal admin, String tab, String ip,
            String userAgent, Supplier<T> call) {
        try {
            T result = call.get();
            auditLogService.recordSuccess(admin.getId(), connection.getApp().getId(), ACTION, TARGET_TYPE, tab,
                    null, result, ip, userAgent);
            return result;
        } catch (RuntimeException e) {
            auditLogService.recordFailure(admin.getId(), connection.getApp().getId(), ACTION, TARGET_TYPE, tab,
                    null, e.getMessage(), ip, userAgent);
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
