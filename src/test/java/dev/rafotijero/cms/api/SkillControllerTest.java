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
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void createUpdateAndDeleteRoundTrip() throws Exception {
        String group = "test-group-" + UUID.randomUUID();
        String name = "test-skill-" + UUID.randomUUID();
        String updatedName = "test-skill-updated-" + UUID.randomUUID();

        String createResponse = mockMvc.perform(post("/api/v1/admin/skills")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillGroup": "%s", "name": "%s", "icon": "test-icon"}
                                """.formatted(group, name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(put("/api/v1/admin/skills/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillGroup": "%s", "name": "%s", "icon": "test-icon"}
                                """.formatted(group, updatedName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));

        mockMvc.perform(delete("/api/v1/admin/skills/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicListGroupsSkillsPreservingDisplayOrder() throws Exception {
        String groupFirst = "test-group-a-" + UUID.randomUUID();
        String groupSecond = "test-group-b-" + UUID.randomUUID();
        String skillFirst = "test-skill-" + UUID.randomUUID();
        String skillSecond = "test-skill-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/skills")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillGroup": "%s", "name": "%s", "displayOrder": -1000000}
                                """.formatted(groupFirst, skillFirst)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/skills")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skillGroup": "%s", "name": "%s", "displayOrder": -999999}
                                """.formatted(groupSecond, skillSecond)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> groups = com.jayway.jsonpath.JsonPath.read(response, "$[*].group");
        assertThat(groups.indexOf(groupFirst)).isLessThan(groups.indexOf(groupSecond));

        List<String> namesInFirstGroup = com.jayway.jsonpath.JsonPath.read(
                response, "$[?(@.group == '%s')].items[*].name".formatted(groupFirst));
        assertThat(namesInFirstGroup).containsExactly(skillFirst);
    }
}
