package com.uap.proiv.jobs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.client.UserApiRepository;
import java.io.IOException;
import java.net.http.HttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(UserControllerIntegrationTest.TestConfig.class)
class UserControllerIntegrationTest {

    private static final String API_KEY = "test-api-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockWebServer mockWebServer;

    @Test
    void getUserById() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 2,
                          "email": "john.doe@example.com",
                          "first_name": "Juan",
                          "last_name": "Perez",
                          "avatar": "https://reqres.in/img/faces/2.jpg"
                        }
                        """));

        mockMvc.perform(get("/api/user/id/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.first_name").value("Juan"))
                .andExpect(jsonPath("$.last_name").value("Perez"))
                .andExpect(jsonPath("$.avatar").value("https://reqres.in/img/faces/2.jpg"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/users/2", request.getPath());
        assertEquals("application/json", request.getHeader("Accept"));
        assertEquals(API_KEY, request.getHeader("X-API-KEY"));
    }

    @Test
    void updateUser() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "name": "Juan",
                          "job": "Garcia"
                        }
                        """));

        mockMvc.perform(post("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 2,
                                  "first_name": "Juan",
                                  "last_name": "Garcia"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/api/users/2", request.getPath());
        assertEquals("application/json", request.getHeader("Accept"));
        assertEquals("application/json", request.getHeader("Content-Type"));
        assertEquals(API_KEY, request.getHeader("X-API-KEY"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean(destroyMethod = "shutdown")
        MockWebServer mockWebServer() throws IOException {
            MockWebServer server = new MockWebServer();
            server.start();
            return server;
        }

        @Bean
        @Primary
        UserApiRepository mockUserApiRepository(ObjectMapper objectMapper, MockWebServer mockWebServer) {
            return new UserApiRepository(
                    HttpClient.newHttpClient(),
                    objectMapper,
                    mockWebServer.url("/api/users").toString(),
                    API_KEY);
        }
    }
}
