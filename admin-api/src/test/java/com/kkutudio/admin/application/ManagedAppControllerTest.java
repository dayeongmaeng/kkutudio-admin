package com.kkutudio.admin.application;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kkutudio.admin.TestcontainersConfiguration;
import com.kkutudio.admin.auth.AdminUser;
import com.kkutudio.admin.auth.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ManagedAppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cookie[] sessionCookies;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        adminUserRepository.save(new AdminUser("apps-tester@kkutudio.com",
                passwordEncoder.encode("correct-password"), "테스터"));

        var result = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"apps-tester@kkutudio.com\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        sessionCookies = result.getResponse().getCookies();
    }

    @Test
    void listReturnsSeededKkoriApp() throws Exception {
        mockMvc.perform(get("/applications").cookie(sessionCookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appCode").value("kkori"))
                .andExpect(jsonPath("$[0].name").value("꼬리"));
    }

    @Test
    void getByAppCodeReturnsDetail() throws Exception {
        mockMvc.perform(get("/applications/kkori").cookie(sessionCookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appCode").value("kkori"))
                .andExpect(jsonPath("$.status").value("DEVELOPMENT"));
    }
}
