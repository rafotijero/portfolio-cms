package dev.rafotijero.cms.api;

import dev.rafotijero.cms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void createAppliesDefaultDisplayOrder() throws Exception {
        String name = "test-cert-" + UUID.randomUUID();
        String body = """
                {"name": "%s", "issuer": "Test Issuer", "issueDate": "2026-01-01"}
                """.formatted(name);

        mockMvc.perform(post("/api/v1/admin/certifications")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void publicListIsOrderedByDisplayOrder() throws Exception {
        String nameFirst = "test-cert-" + UUID.randomUUID();
        String nameSecond = "test-cert-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/certifications")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "issuer": "Issuer", "issueDate": "2026-01-01", "displayOrder": -1000000}
                                """.formatted(nameFirst)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/certifications")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "issuer": "Issuer", "issueDate": "2026-01-01", "displayOrder": -999999}
                                """.formatted(nameSecond)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(get("/api/v1/certifications"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> names = com.jayway.jsonpath.JsonPath.read(response, "$[*].name");
        assertThat(names.indexOf(nameFirst)).isLessThan(names.indexOf(nameSecond));
    }

    @Test
    void updateAndDeleteRoundTrip() throws Exception {
        String name = "test-cert-" + UUID.randomUUID();
        String updatedName = "test-cert-updated-" + UUID.randomUUID();

        String createResponse = mockMvc.perform(post("/api/v1/admin/certifications")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "issuer": "Issuer", "issueDate": "2026-01-01"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(put("/api/v1/admin/certifications/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "issuer": "Issuer", "issueDate": "2026-01-01"}
                                """.formatted(updatedName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));

        mockMvc.perform(delete("/api/v1/admin/certifications/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        String afterDelete = mockMvc.perform(get("/api/v1/certifications"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> namesAfterDelete = com.jayway.jsonpath.JsonPath.read(afterDelete, "$[*].name");
        assertThat(namesAfterDelete).doesNotContain(updatedName);
    }
}
