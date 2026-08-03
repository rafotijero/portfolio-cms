package dev.rafotijero.cms.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        String body = """
                {"username": "%s", "password": "%s"}
                """.formatted(adminUsername, adminPassword);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordReturnsGenericUnauthorized() throws Exception {
        String body = """
                {"username": "%s", "password": "definitely-wrong-password"}
                """.formatted(adminUsername);

        String errorMessage = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getErrorMessage();

        assertThat(errorMessage).isEqualTo("Credenciales invalidas");
    }

    @Test
    void loginWithUnknownUsernameReturnsSameGenericUnauthorized() throws Exception {
        String body = """
                {"username": "no-such-user-xyz", "password": "whatever"}
                """;

        String errorMessage = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getErrorMessage();

        assertThat(errorMessage).isEqualTo("Credenciales invalidas");
    }
}
