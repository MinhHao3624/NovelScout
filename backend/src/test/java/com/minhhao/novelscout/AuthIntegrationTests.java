package com.minhhao.novelscout;

import com.minhhao.novelscout.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registerRejectsDuplicateEmailAndUsername() throws Exception {
        register("reader@example.com", "reader_one", "Người đọc");

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("reader@example.com", "reader_two", "Người khác")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_EXISTS"));

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("other@example.com", "reader_one", "Người khác")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_EXISTS"));
    }

    @Test
    void loginCreatesSessionAndAllowsProfileUpdate() throws Exception {
        register("reader@example.com", "reader_one", "Người đọc");

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"reader_one\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));

        HttpSession session = mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"READER@EXAMPLE.COM\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("reader_one"))
                .andExpect(jsonPath("$.roles[0]").value("READER"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("reader@example.com"));

        mockMvc.perform(put("/api/users/me/profile").session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Độc giả mới\",\"avatarUrl\":\"https://example.com/avatar.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Độc giả mới"));
    }

    @Test
    void changingPasswordInvalidatesCurrentSession() throws Exception {
        register("reader@example.com", "reader_one", "Người đọc");
        var session = (org.springframework.mock.web.MockHttpSession) mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"reader_one\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);

        mockMvc.perform(put("/api/users/me/password").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"Password123\",\"newPassword\":\"NewPassword456\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"reader_one\",\"password\":\"NewPassword456\"}"))
                .andExpect(status().isOk());
    }

    private void register(String email, String username, String displayName) throws Exception {
        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, username, displayName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));
    }

    private String registerJson(String email, String username, String displayName) {
        return """
                {"email":"%s","username":"%s","password":"Password123","displayName":"%s"}
                """.formatted(email, username, displayName);
    }
}
