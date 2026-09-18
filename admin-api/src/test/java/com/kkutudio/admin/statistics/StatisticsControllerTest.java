package com.kkutudio.admin.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kkutudio.admin.TestcontainersConfiguration;
import com.kkutudio.admin.audit.AuditLogRepository;
import com.kkutudio.admin.auth.AdminUser;
import com.kkutudio.admin.auth.AdminUserRepository;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StatisticsControllerTest {

    private static HttpServer stubServer;

    @BeforeAll
    static void startStubKkoriApi() throws IOException {
        stubServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        stubServer.createContext("/dashboard/overview", exchange -> respondJson(exchange, 200, """
                {"totalMembers":10,"dau":1,"wau":2,"mau":3,"stickiness":0.33,"todayRecordCount":5,
                "activeUnitTrend":[{"bucketStart":"2026-03-01","value":1}]}"""));
        stubServer.createContext("/dashboard/members", exchange -> respondJson(exchange, 200, """
                {"totalMembers":10,"withdrawnMembers":1,"signupTrend":[],"withdrawalTrend":[],
                "byProvider":{"GOOGLE":5},"byStatus":{"ACTIVE":10}}"""));
        stubServer.createContext("/dashboard/pets", exchange -> respondJson(exchange, 200, """
                {"totalPets":5,"newPetTrend":[],"bySpecies":{"DOG":3},"petsPerMemberDistribution":{"0":2}}"""));
        stubServer.createContext("/dashboard/records", exchange -> respondJson(exchange, 200, """
                {"photoTrend":[],"logTrend":[],"activeUnitTrend":[],"engagementTrend":[],"cumulativeTrend":[],
                "streak":{"avgCurrentStreakDays":1.5,"maxCurrentStreakDays":3,"distribution":{"0":1}},
                "featureCombination":{"BOTH":2}}"""));
        stubServer.createContext("/dashboard/conversion", exchange -> respondJson(exchange, 200, """
                {"signupToFirstPet":{"avgHours":10.0,"medianHours":9.0,"sampleCount":2},
                "petToFirstRecord":{"avgHours":1.0,"medianHours":1.0,"sampleCount":1},
                "retention":[{"cohortWeekStart":"2026-03-02","cohortSize":5,"w1":0.5,"w2":null,"w4":null,"w8":null}]}"""));
        stubServer.start();
    }

    @AfterAll
    static void stopStubKkoriApi() {
        stubServer.stop(0);
    }

    private static void respondJson(com.sun.net.httpserver.HttpExchange exchange, int status, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Cookie[] sessionCookies;

    @BeforeEach
    void setUp() throws Exception {
        int port = stubServer.getAddress().getPort();
        jdbcTemplate.update("UPDATE app_connection SET base_url = ? WHERE app_id = "
                + "(SELECT id FROM managed_app WHERE app_code = 'kkori')", "http://localhost:" + port);

        adminUserRepository.save(new AdminUser("statistics-tester@kkutudio.com",
                passwordEncoder.encode("correct-password"), "테스터"));

        var result = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"statistics-tester@kkutudio.com\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        sessionCookies = result.getResponse().getCookies();
    }

    @Test
    void overviewProxiesToAppAdminApiAndRecordsAudit() throws Exception {
        long auditCountBefore = auditLogRepository.count();

        mockMvc.perform(get("/apps/kkori/statistics/overview")
                        .cookie(sessionCookies)
                        .param("from", "2026-03-01").param("to", "2026-03-07").param("unit", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(10))
                .andExpect(jsonPath("$.dau").value(1))
                .andExpect(jsonPath("$.activeUnitTrend[0].bucketStart").value("2026-03-01"));

        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
    }

    @Test
    void membersTabProxiesAndRecordsAudit() throws Exception {
        long auditCountBefore = auditLogRepository.count();

        mockMvc.perform(get("/apps/kkori/statistics/members")
                        .cookie(sessionCookies)
                        .param("from", "2026-03-01").param("to", "2026-03-07").param("unit", "WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byProvider.GOOGLE").value(5));

        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
    }

    @Test
    void petsTabProxiesAndRecordsAudit() throws Exception {
        mockMvc.perform(get("/apps/kkori/statistics/pets")
                        .cookie(sessionCookies)
                        .param("from", "2026-03-01").param("to", "2026-03-07").param("unit", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPets").value(5));
    }

    @Test
    void recordsTabProxiesAndRecordsAudit() throws Exception {
        mockMvc.perform(get("/apps/kkori/statistics/records")
                        .cookie(sessionCookies)
                        .param("from", "2026-03-01").param("to", "2026-03-07").param("unit", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streak.maxCurrentStreakDays").value(3));
    }

    @Test
    void conversionTabProxiesAndRecordsAudit() throws Exception {
        mockMvc.perform(get("/apps/kkori/statistics/conversion")
                        .cookie(sessionCookies)
                        .param("from", "2026-03-01").param("to", "2026-03-07").param("unit", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retention[0].w1").value(0.5));
    }

    @Test
    void unknownAppCodeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/apps/unknown-app/statistics/overview")
                        .cookie(sessionCookies)
                        .param("from", "2026-03-01").param("to", "2026-03-07"))
                .andExpect(status().isNotFound());
    }
}
