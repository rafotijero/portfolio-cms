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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void createWithoutOptionalFieldsAppliesDefaults() throws Exception {
        String slug = "test-project-" + UUID.randomUUID();
        String body = """
                {"name": "Test Project", "slug": "%s", "status": "DRAFT"}
                """.formatted(slug);

        mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.featured").value(false))
                .andExpect(jsonPath("$.displayOrder").value(0));
    }

    @Test
    void adminListAndDetail() throws Exception {
        String slug = "test-project-" + UUID.randomUUID();
        String body = """
                {"name": "Test Project", "slug": "%s", "status": "DRAFT"}
                """.formatted(slug);

        String response = mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/v1/admin/projects")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == '%s')]".formatted(slug)).exists());

        mockMvc.perform(get("/api/v1/admin/projects/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void publicListOnlyReturnsPublishedProjects() throws Exception {
        String draftSlug = "test-project-" + UUID.randomUUID();
        String publishedSlug = "test-project-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Draft Project", "slug": "%s", "status": "DRAFT"}
                                """.formatted(draftSlug)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Published Project", "slug": "%s", "status": "PUBLISHED"}
                                """.formatted(publishedSlug)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == '%s')]".formatted(publishedSlug)).exists())
                .andExpect(jsonPath("$[?(@.slug == '%s')]".formatted(draftSlug)).isEmpty());
    }

    @Test
    void publicDetailReturnsNotFoundForDraftAndUnknownSlug() throws Exception {
        String draftSlug = "test-project-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Draft Project", "slug": "%s", "status": "DRAFT"}
                                """.formatted(draftSlug)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/projects/{slug}", draftSlug))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/projects/{slug}", "no-such-slug-" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAndDeleteRoundTrip() throws Exception {
        String slug = "test-project-" + UUID.randomUUID();

        String createResponse = mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test Project", "slug": "%s", "status": "DRAFT"}
                                """.formatted(slug)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(put("/api/v1/admin/projects/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated Project", "slug": "%s", "status": "DRAFT"}
                                """.formatted(slug)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"));

        mockMvc.perform(delete("/api/v1/admin/projects/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/projects/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNotFound());
    }
}
