package com.kkutudio.admin.statistics;

import com.kkutudio.admin.auth.AdminUserPrincipal;
import com.kkutudio.admin.integration.AppAdminClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apps/{appCode}/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public AppAdminClient.DashboardOverviewResponse overview(@PathVariable String appCode,
            @RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "DAY") String unit,
            Authentication authentication, HttpServletRequest request) {
        return statisticsService.getOverview(appCode, from, to, unit, principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/members")
    public AppAdminClient.DashboardMembersResponse members(@PathVariable String appCode,
            @RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "DAY") String unit,
            Authentication authentication, HttpServletRequest request) {
        return statisticsService.getMembers(appCode, from, to, unit, principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/pets")
    public AppAdminClient.DashboardPetsResponse pets(@PathVariable String appCode,
            @RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "DAY") String unit,
            Authentication authentication, HttpServletRequest request) {
        return statisticsService.getPets(appCode, from, to, unit, principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/records")
    public AppAdminClient.DashboardRecordsResponse records(@PathVariable String appCode,
            @RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "DAY") String unit,
            Authentication authentication, HttpServletRequest request) {
        return statisticsService.getRecords(appCode, from, to, unit, principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/conversion")
    public AppAdminClient.DashboardConversionResponse conversion(@PathVariable String appCode,
            @RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "DAY") String unit,
            Authentication authentication, HttpServletRequest request) {
        return statisticsService.getConversion(appCode, from, to, unit, principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    private AdminUserPrincipal principal(Authentication authentication) {
        return (AdminUserPrincipal) authentication.getPrincipal();
    }
}
