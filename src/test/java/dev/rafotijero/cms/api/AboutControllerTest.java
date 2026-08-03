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
class AboutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void publicListIsOrderedByDisplayOrder() throws Exception {
        String contentFirst = "test-paragraph-" + UUID.randomUUID();
        String contentSecond = "test-paragraph-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/about")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "%s", "displayOrder": -1000000}
                                """.formatted(contentFirst)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/about")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "%s", "displayOrder": -999999}
                                """.formatted(contentSecond)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(get("/api/v1/about"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> paragraphs = com.jayway.jsonpath.JsonPath.read(response, "$");
        assertThat(paragraphs.indexOf(contentFirst)).isLessThan(paragraphs.indexOf(contentSecond));
    }

    @Test
    void updateAndDeleteRoundTrip() throws Exception {
        String content = "test-paragraph-" + UUID.randomUUID();
        String updatedContent = "test-paragraph-updated-" + UUID.randomUUID();

        String createResponse = mockMvc.perform(post("/api/v1/admin/about")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "%s"}
                                """.formatted(content)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(put("/api/v1/admin/about/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "%s"}
                                """.formatted(updatedContent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(updatedContent));

        mockMvc.perform(delete("/api/v1/admin/about/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        String afterDelete = mockMvc.perform(get("/api/v1/about"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> paragraphsAfterDelete = com.jayway.jsonpath.JsonPath.read(afterDelete, "$");
        assertThat(paragraphsAfterDelete).doesNotContain(updatedContent);
    }
}
