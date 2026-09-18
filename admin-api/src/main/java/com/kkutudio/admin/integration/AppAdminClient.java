package com.kkutudio.admin.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 각 앱(managed_app)의 {@code /internal/admin} API와 통신하는 클라이언트.
 * {@link AppAdminClientFactory}가 앱별 {@link org.springframework.web.client.RestClient}를 기반으로
 * {@code HttpServiceProxyFactory}로 구현체를 생성한다.
 *
 * <p>엔드포인트 계약은 아직 실제 앱과 합의되지 않은 잠정안이며, 연동 시점에 맞춰 조정될 수 있다.
 */
public interface AppAdminClient {

    @GetExchange("/health")
    AppHealthResponse checkHealth();

    @GetExchange("/members")
    MemberPageResponse getMembers(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetExchange("/members/{memberId}")
    MemberDetailResponse getMember(@PathVariable String memberId);

    @PostExchange("/members/{memberId}/suspend")
    void suspendMember(@PathVariable String memberId, @RequestBody SuspendMemberRequest request);

    @PostExchange("/members/{memberId}/force-logout")
    void forceLogoutMember(@PathVariable String memberId);

    @GetExchange("/dashboard/overview")
    DashboardOverviewResponse getDashboardOverview(@RequestParam String from, @RequestParam String to,
            @RequestParam String unit);

    @GetExchange("/dashboard/members")
    DashboardMembersResponse getDashboardMembers(@RequestParam String from, @RequestParam String to,
            @RequestParam String unit);

    @GetExchange("/dashboard/pets")
    DashboardPetsResponse getDashboardPets(@RequestParam String from, @RequestParam String to,
            @RequestParam String unit);

    @GetExchange("/dashboard/records")
    DashboardRecordsResponse getDashboardRecords(@RequestParam String from, @RequestParam String to,
            @RequestParam String unit);

    @GetExchange("/dashboard/conversion")
    DashboardConversionResponse getDashboardConversion(@RequestParam String from, @RequestParam String to,
            @RequestParam String unit);

    record AppHealthResponse(String status) {
    }

    record MemberSummaryResponse(String memberId, String nickname, String email, String status, Instant joinedAt) {
    }

    record MemberDetailResponse(String memberId, String nickname, String email, String status, Instant joinedAt,
            Instant lastActiveAt) {
    }

    record MemberPageResponse(List<MemberSummaryResponse> content, int page, int size, long totalElements,
            int totalPages) {
    }

    record SuspendMemberRequest(String reason) {
    }

    // 대시보드 응답 DTO. kkori-api의 com.kkori.api.admin.dto.response.dashboard.* record와 1:1로 대응한다.

    record DashboardTimeSeriesPoint(LocalDate bucketStart, long value) {
    }

    record DashboardEngagementPoint(LocalDate bucketStart, long activeUnits, long eligibleUnits, double rate) {
    }

    record DashboardStreakSummary(double avgCurrentStreakDays, int maxCurrentStreakDays, Map<String, Long> distribution) {
    }

    record DashboardDurationStats(double avgHours, double medianHours, long sampleCount) {
    }

    record DashboardCohortRetention(LocalDate cohortWeekStart, long cohortSize, Double w1, Double w2, Double w4,
            Double w8) {
    }

    record DashboardOverviewResponse(long totalMembers, long dau, long wau, long mau, double stickiness,
            long todayRecordCount, List<DashboardTimeSeriesPoint> activeUnitTrend) {
    }

    record DashboardMembersResponse(long totalMembers, long withdrawnMembers,
            List<DashboardTimeSeriesPoint> signupTrend, List<DashboardTimeSeriesPoint> withdrawalTrend,
            Map<String, Long> byProvider, Map<String, Long> byStatus) {
    }

    record DashboardPetsResponse(long totalPets, List<DashboardTimeSeriesPoint> newPetTrend,
            Map<String, Long> bySpecies, Map<String, Long> petsPerMemberDistribution) {
    }

    record DashboardRecordsResponse(List<DashboardTimeSeriesPoint> photoTrend, List<DashboardTimeSeriesPoint> logTrend,
            List<DashboardTimeSeriesPoint> activeUnitTrend, List<DashboardEngagementPoint> engagementTrend,
            List<DashboardTimeSeriesPoint> cumulativeTrend, DashboardStreakSummary streak,
            Map<String, Long> featureCombination) {
    }

    record DashboardConversionResponse(DashboardDurationStats signupToFirstPet,
            DashboardDurationStats petToFirstRecord, List<DashboardCohortRetention> retention) {
    }
}
