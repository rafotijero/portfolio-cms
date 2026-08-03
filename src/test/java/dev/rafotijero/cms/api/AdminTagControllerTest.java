package dev.rafotijero.cms.api;

import dev.rafotijero.cms.repository.TagRepository;
import dev.rafotijero.cms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
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
class AdminTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TagRepository tagRepository;

    private String adminToken() {
        return jwtService.generate("test-admin", "ADMIN").token();
    }

    private String authHeader() {
        return "Bearer " + adminToken();
    }

    @Test
    void createListGetUpdateAndDeleteTag() throws Exception {
        String name = "test-tag-" + UUID.randomUUID();
        String slug = "test-slug-" + UUID.randomUUID();

        String createBody = """
                {"name": "%s", "slug": "%s"}
                """.formatted(name, slug);

        String createResponse = mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.slug").value(slug))
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(get("/api/v1/admin/tags")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/tags/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        // tags.name es VARCHAR(50); un UUID completo (36) + este prefijo se pasaria del limite.
        String updatedName = "test-tag-upd-" + UUID.randomUUID().toString().substring(0, 8);
        String updateBody = """
                {"name": "%s", "slug": "%s"}
                """.formatted(updatedName, slug);

        mockMvc.perform(put("/api/v1/admin/tags/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));

        mockMvc.perform(delete("/api/v1/admin/tags/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/tags/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createWithDuplicateNameReturnsConflict() throws Exception {
        // NOT_SUPPORTED: the unique-constraint check only happens when Postgres actually
        // executes the INSERT, which needs a real per-request commit (the class-level
        // @Transactional rollback would defer both inserts past the point where the
        // conflict could be observed). The created row is deleted manually below since
        // there's no rollback safety net for this test.
        String name = "test-tag-" + UUID.randomUUID();
        String slug1 = "test-slug-" + UUID.randomUUID();
        String slug2 = "test-slug-" + UUID.randomUUID();

        String firstBody = """
                {"name": "%s", "slug": "%s"}
                """.formatted(name, slug1);

        String createResponse = mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        try {
            String duplicateBody = """
                    {"name": "%s", "slug": "%s"}
                    """.formatted(name, slug2);

            mockMvc.perform(post("/api/v1/admin/tags")
                            .header("Authorization", authHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicateBody))
                    .andExpect(status().isConflict());
        } finally {
            tagRepository.deleteById(UUID.fromString(id));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void nameAndSlugCanBeReusedAfterSoftDelete() throws Exception {
        // Same NOT_SUPPORTED reasoning as createWithDuplicateNameReturnsConflict: the
        // soft-delete's UPDATE (via @SQLDelete) also needs a real per-request flush to
        // actually free up the partial unique index before the second create runs.
        String name = "test-tag-" + UUID.randomUUID();
        String slug = "test-slug-" + UUID.randomUUID();

        String firstBody = """
                {"name": "%s", "slug": "%s"}
                """.formatted(name, slug);

        String firstResponse = mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String firstId = com.jayway.jsonpath.JsonPath.read(firstResponse, "$.id");

        mockMvc.perform(delete("/api/v1/admin/tags/{id}", firstId)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        // El primer tag ya quedo soft-deleted (deleted_at seteado) por el DELETE de arriba;
        // solo hace falta limpiar el segundo, que es el que queda "activo".
        String secondResponse = mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondId = com.jayway.jsonpath.JsonPath.read(secondResponse, "$.id");
        tagRepository.deleteById(UUID.fromString(secondId));
    }

    @Test
    void createWithBlankNameReturnsBadRequest() throws Exception {
        String body = """
                {"name": "", "slug": "test-slug-%s"}
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/admin/tags")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/tags/{id}", UUID.randomUUID())
                        .header("Authorization", authHeader()))
                .andExpect(status().isNotFound());
    }
}
