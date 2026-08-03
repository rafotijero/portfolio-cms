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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    private String createTag() throws Exception {
        String name = "test-tag-" + UUID.randomUUID();
        String body = """
                {"name": "%s", "slug": "%s"}
                """.formatted(name, name);

        String response = mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(response, "$.id");
    }

    private String postBody(String slug, String status, String tagId) {
        String tagIdsJson = tagId == null ? "[]" : "[\"%s\"]".formatted(tagId);
        return """
                {"title": "Test Post", "slug": "%s", "status": "%s", "summary": "summary",
                 "content": "content", "tagIds": %s}
                """.formatted(slug, status, tagIdsJson);
    }

    @Test
    void createWithValidTagIdSucceeds() throws Exception {
        String tagId = createTag();
        String slug = "test-post-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody(slug, "DRAFT", tagId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void createWithUnknownTagIdReturnsBadRequest() throws Exception {
        String slug = "test-post-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody(slug, "DRAFT", UUID.randomUUID().toString())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminListIncludesDraftRows() throws Exception {
        String slug = "test-post-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody(slug, "DRAFT", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/admin/posts")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.slug == '%s')]".formatted(slug)).exists());
    }

    @Test
    void publicListOnlyReturnsPublishedPosts() throws Exception {
        String draftSlug = "test-post-" + UUID.randomUUID();
        String publishedSlug = "test-post-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody(draftSlug, "DRAFT", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Test Post", "slug": "%s", "status": "PUBLISHED", "summary": "s",
                                 "content": "c", "publishedAt": "2026-01-01T00:00:00Z"}
                                """.formatted(publishedSlug)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/posts").param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.slug == '%s')]".formatted(publishedSlug)).exists())
                .andExpect(jsonPath("$.content[?(@.slug == '%s')]".formatted(draftSlug)).isEmpty());
    }

    @Test
    void publicDetailReturnsNotFoundForDraftAndUnknownSlug() throws Exception {
        String draftSlug = "test-post-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody(draftSlug, "DRAFT", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/posts/{slug}", draftSlug))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/posts/{slug}", "no-such-slug-" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAndDeleteRoundTrip() throws Exception {
        String slug = "test-post-" + UUID.randomUUID();

        String createResponse = mockMvc.perform(post("/api/v1/admin/posts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody(slug, "DRAFT", null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(put("/api/v1/admin/posts/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Updated Title", "slug": "%s", "status": "DRAFT", "summary": "s",
                                 "content": "c"}
                                """.formatted(slug)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        mockMvc.perform(delete("/api/v1/admin/posts/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/posts/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNotFound());
    }
}
