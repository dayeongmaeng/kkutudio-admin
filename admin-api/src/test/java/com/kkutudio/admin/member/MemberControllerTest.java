package com.kkutudio.admin.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kkutudio.admin.TestcontainersConfiguration;
import com.kkutudio.admin.application.AppConnectionRepository;
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
class MemberControllerTest {

    private static HttpServer stubServer;

    @BeforeAll
    static void startStubKkoriApi() throws IOException {
        stubServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        stubServer.createContext("/members", exchange -> {
            if ("/members".equals(exchange.getRequestURI().getPath()) && "GET".equals(exchange.getRequestMethod())) {
                respondJson(exchange, 200, """
                        {"content":[{"memberId":"m1","nickname":"nick","email":"m1@kkori.example","status":"ACTIVE",
                        "joinedAt":"2026-01-01T00:00:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}""");
            } else {
                respondJson(exchange, 404, "{}");
            }
        });
        stubServer.createContext("/members/m1/suspend", exchange -> respondJson(exchange, 200, "{}"));
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
    private AppConnectionRepository appConnectionRepository;

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

        adminUserRepository.save(new AdminUser("member-tester@kkutudio.com",
                passwordEncoder.encode("correct-password"), "테스터"));

        var result = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member-tester@kkutudio.com\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        sessionCookies = result.getResponse().getCookies();
    }

    @Test
    void listMembersProxiesToAppAdminApiAndRecordsAudit() throws Exception {
        long auditCountBefore = auditLogRepository.count();

        mockMvc.perform(get("/apps/kkori/members").cookie(sessionCookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].memberId").value("m1"))
                .andExpect(jsonPath("$.totalElements").value(1));

        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
    }

    @Test
    void suspendMemberProxiesToAppAdminApiAndRecordsAudit() throws Exception {
        long auditCountBefore = auditLogRepository.count();

        mockMvc.perform(post("/apps/kkori/members/m1/suspend")
                        .cookie(sessionCookies)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"약관 위반\"}"))
                .andExpect(status().isOk());

        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
    }

    @Test
    void unknownAppCodeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/apps/unknown-app/members").cookie(sessionCookies))
                .andExpect(status().isNotFound());
    }
}
