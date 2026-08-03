package dev.rafotijero.cms.api;

import dev.rafotijero.cms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String validAdminToken() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void publicEndpointWithoutTokenReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/tags"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/tags")
                        .header("Authorization", "Bearer this-is-not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointWithValidTokenReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/tags")
                        .header("Authorization", validAdminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void authEndpointOtherThanLoginWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/whoami"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authEndpointOtherThanLoginWithValidTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/auth/whoami")
                        .header("Authorization", validAdminToken()))
                .andExpect(status().isForbidden());
    }
}
