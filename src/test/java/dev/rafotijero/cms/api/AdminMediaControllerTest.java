package dev.rafotijero.cms.api;

import dev.rafotijero.cms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminMediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrl;

    private String authHeader() {
        return "Bearer " + jwtService.generate("test-admin", "ADMIN").token();
    }

    @Test
    void uploadStoresFileAndReturnsPopulatedTimestamp() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-upload.txt", "text/plain", "hello world".getBytes());

        mockMvc.perform(multipart("/api/v1/admin/media")
                        .file(file)
                        .header("Authorization", authHeader()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("test-upload.txt"))
                .andExpect(jsonPath("$.contentType").value("text/plain"))
                .andExpect(jsonPath("$.sizeBytes").value("hello world".getBytes().length))
                .andExpect(jsonPath("$.uploadedAt").isNotEmpty())
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.startsWith(publicUrl)));

        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void uploadWithEmptyFileReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/api/v1/admin/media")
                        .file(file)
                        .header("Authorization", authHeader()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReflectsUploadedFilesNewestFirst() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "list-test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/admin/media")
                        .file(file)
                        .header("Authorization", authHeader()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/admin/media")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("list-test.txt"));
    }

    @Test
    void deleteRemovesFromR2AndDatabase() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "delete-test.txt", "text/plain", "content".getBytes());

        String createResponse = mockMvc.perform(multipart("/api/v1/admin/media")
                        .file(file)
                        .header("Authorization", authHeader()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");
        String url = com.jayway.jsonpath.JsonPath.read(createResponse, "$.url");
        String expectedKey = url.replace(publicUrl + "/", "");

        mockMvc.perform(delete("/api/v1/admin/media/{id}", id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNoContent());

        verify(s3Client).deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(expectedKey)
                .build());

        String listResponse = mockMvc.perform(get("/api/v1/admin/media")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.List<String> ids = com.jayway.jsonpath.JsonPath.read(listResponse, "$[*].id");
        assertThat(ids).doesNotContain(id);
    }
}
