package com.example.jwtsecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "Password123"))))
                .andExpect(status().isCreated());
        return login(username, "Password123");
    }

    private String newUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void registeredUserGetsOnlyRoleUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", newUsername(), "password", "Password123",
                                "roles", "ROLE_ADMIN"))))   // attempt to self-assign admin is ignored
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void duplicateUsernameReturnsConflict() throws Exception {
        String username = newUsername();
        registerAndLogin(username);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "Password123"))))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        String username = newUsername();
        registerAndLogin(username);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "WrongPassword1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/api/players")).andExpect(status().isUnauthorized());
    }

    @Test
    void userCanReadButNotCreatePlayers_adminCan() throws Exception {
        String userToken = registerAndLogin(newUsername());
        String adminToken = login("admin", "Admin@12345");
        String player = json(Map.of("name", "Sadio Mane", "jerseyNumber", 10, "position", "Forward"));

        mockMvc.perform(get("/api/players").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/players").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(player))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/players").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(player))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sadio Mane"));
    }

    @Test
    void invalidPlayerPayloadReturnsBadRequest() throws Exception {
        String adminToken = login("admin", "Admin@12345");
        mockMvc.perform(post("/api/players").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "", "jerseyNumber", 500))))
                .andExpect(status().isBadRequest());
    }
}
