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
class ExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void createWithoutEndDateRepresentsCurrentPosition() throws Exception {
        String company = "test-company-" + UUID.randomUUID();
        String body = """
                {"role": "Engineer", "company": "%s", "startDate": "2026-01-01", "summary": "summary"}
                """.formatted(company);

        mockMvc.perform(post("/api/v1/admin/experience")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company").value(company))
                .andExpect(jsonPath("$.endDate").doesNotExist());
    }

    @Test
    void publicListIsOrderedByDisplayOrder() throws Exception {
        String companyFirst = "test-company-" + UUID.randomUUID();
        String companySecond = "test-company-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/experience")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Engineer", "company": "%s", "startDate": "2026-01-01",
                                 "summary": "s", "displayOrder": -1000000}
                                """.formatted(companyFirst)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/experience")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Engineer", "company": "%s", "startDate": "2026-01-01",
                                 "summary": "s", "displayOrder": -999999}
                                """.formatted(companySecond)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(get("/api/v1/experience"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> companies = com.jayway.jsonpath.JsonPath.read(response, "$[*].company");
        assertThat(companies.indexOf(companyFirst)).isLessThan(companies.indexOf(companySecond));
    }

    @Test
    void updateAndDeleteRoundTrip() throws Exception {
        String company = "test-company-" + UUID.randomUUID();
        String updatedRole = "Senior Engineer " + UUID.randomUUID();

        String createResponse = mockMvc.perform(post("/api/v1/admin/experience")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Engineer", "company": "%s", "startDate": "2026-01-01", "summary": "s"}
                                """.formatted(company)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(put("/api/v1/admin/experience/{id}", id)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "%s", "company": "%s", "startDate": "2026-01-01", "summary": "s"}
                                """.formatted(updatedRole, company)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(updatedRole));

        mockMvc.perform(delete("/api/v1/admin/experience/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        String afterDelete = mockMvc.perform(get("/api/v1/experience"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> rolesAfterDelete = com.jayway.jsonpath.JsonPath.read(afterDelete, "$[*].role");
        assertThat(rolesAfterDelete).doesNotContain(updatedRole);
    }
}
