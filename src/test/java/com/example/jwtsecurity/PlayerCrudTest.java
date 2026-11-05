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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerCrudTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    private String json(Object body) throws Exception {
        return om.writeValueAsString(body);
    }

    private String login(String username, String password) throws Exception {
        String res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + om.readTree(res).get("token").asText();
    }

    @Test
    void adminCanCreateReadUpdateAndDeletePlayer() throws Exception {
        String auth = login("admin", "Admin@12345");

        String created = mockMvc.perform(post("/api/players")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Sadio Mane", "jerseyNumber", 10, "position", "Forward"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/players/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sadio Mane"));

        mockMvc.perform(put("/api/players/" + id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Sadio Mane", "jerseyNumber", 11, "position", "Winger"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jerseyNumber").value(11));

        mockMvc.perform(delete("/api/players/" + id).header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/players/" + id).header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownPlayerReturnsNotFound() throws Exception {
        String auth = login("admin", "Admin@12345");
        mockMvc.perform(get("/api/players/999999").header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanListUsersAndPasswordsAreNeverExposed() throws Exception {
        String auth = login("admin", "Admin@12345");
        mockMvc.perform(get("/api/users").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void userCanSeeOwnProfileAndChangePassword() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "Password123"))))
                .andExpect(status().isCreated());
        String auth = login(username, "Password123");

        mockMvc.perform(get("/api/users/me").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("newPassword", "NewPassword123"))))
                .andExpect(status().isNoContent());

        login(username, "NewPassword123");   // new password works

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "Password123"))))
                .andExpect(status().isUnauthorized());   // old password no longer works
    }
}