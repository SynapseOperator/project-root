package com.yuelutraffic.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void studentLoginCreatesPrivatePublicUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"20260001\",\"privacyAcknowledged\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newUser", is(true)))
                .andExpect(jsonPath("$.user.publicCode", containsString("User-")))
                .andExpect(jsonPath("$.user.reputationScore", is(50)))
                .andExpect(jsonPath("$.privacyNotice", containsString("not public")))
                .andExpect(content().string(not(containsString("20260001"))))
                .andExpect(content().string(not(containsString("studentNumberHash"))))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String token = body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicCode", containsString("User-")))
                .andExpect(content().string(not(containsString("20260001"))));
    }

    @Test
    void privacyAcknowledgementIsRequired() throws Exception {
        mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"20260002\",\"privacyAcknowledged\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("privacyAcknowledged")));
    }

    @Test
    void repeatedLoginReturnsExistingUser() throws Exception {
        String request = "{\"studentNumber\":\"20260003\",\"privacyAcknowledged\":true}";
        mockMvc.perform(post("/api/v1/auth/student").contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newUser", is(true)));

        mockMvc.perform(post("/api/v1/auth/student").contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newUser", is(false)));
    }

    @Test
    void leaderboardDoesNotExposeStudentNumbers() throws Exception {
        mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"20260004\",\"privacyAcknowledged\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User-")))
                .andExpect(content().string(not(containsString("20260004"))))
                .andExpect(content().string(not(containsString("studentNumberHash"))));
    }
}
