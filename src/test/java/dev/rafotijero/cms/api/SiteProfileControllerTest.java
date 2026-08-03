package dev.rafotijero.cms.api;

import dev.rafotijero.cms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The singleton {@code site_profile} row already exists in this shared database (it's the live
 * deployed CMS's config). This test only exercises the update path — reusing @Transactional rollback
 * to restore the original row afterward — rather than deleting/recreating the real row.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SiteProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void updateAppliesFieldsAndPublicGetReflectsThem() throws Exception {
        String name = "Test Name " + UUID.randomUUID();
        String email = "test-" + UUID.randomUUID() + "@example.com";

        String body = """
                {"name": "%s", "role": "Test Role", "tagline": "Test Tagline", "email": "%s"}
                """.formatted(name, email);

        mockMvc.perform(put("/api/v1/admin/site")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(get("/api/v1/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));
    }
}
