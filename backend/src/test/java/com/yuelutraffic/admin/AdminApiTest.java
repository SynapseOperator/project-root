package com.yuelutraffic.admin;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
class AdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminCanModerateAndRestrictPosting() throws Exception {
        Credentials user = login("20263001");
        Credentials admin = login("ADMIN-DEMO");

        mockMvc.perform(get("/api/v1/admin/review-queue")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isForbidden());

        String reportId = extract(createReport(user.token()).andExpect(status().isOk()).andReturn(), "id");
        mockMvc.perform(post("/api/v1/admin/reports/" + reportId + "/moderate")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType("application/json")
                        .content("{\"status\":\"HIDDEN\",\"reason\":\"Invalid local report\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));

        String accidentId = extract(createAccident(user.token()).andExpect(status().isOk()).andReturn(), "id");
        mockMvc.perform(post("/api/v1/admin/accidents/" + accidentId + "/moderate")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType("application/json")
                        .content("{\"status\":\"HIDDEN\",\"reason\":\"Accident board abuse\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));

        String banUntil = Instant.now().plusSeconds(3600).toString();
        mockMvc.perform(post("/api/v1/admin/users/" + user.userId() + "/restrictions")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType("application/json")
                        .content("{\"postingBanUntil\":\"" + banUntil + "\",\"reason\":\"Repeated malicious submissions\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postingBanUntil").value(containsString("Z")));

        createReport(user.token())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("restricted")));
    }

    private Credentials login(String studentNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"" + studentNumber + "\",\"privacyAcknowledged\":true}"))
                .andExpect(status().isOk())
                .andReturn();
        return new Credentials(extract(result, "accessToken"), extractNestedUserId(result));
    }

    private org.springframework.test.web.servlet.ResultActions createReport(String token) throws Exception {
        return mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                          "type":"CONGESTION",
                          "latitude":28.1703,
                          "longitude":112.9388,
                          "locationLabel":"Lushan South Road",
                          "description":"Local report",
                          "initialCredibility":50
                        }
                        """));
    }

    private org.springframework.test.web.servlet.ResultActions createAccident(String token) throws Exception {
        return mockMvc.perform(post("/api/v1/accidents")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                          "latitude":28.1703,
                          "longitude":112.9388,
                          "locationLabel":"Lushan South Road",
                          "description":"Minor incident"
                        }
                        """));
    }

    private String extract(MvcResult result, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceFirst(".*?\"" + field + "\":\"([^\"]+)\".*", "$1");
    }

    private String extractNestedUserId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceFirst(".*?\"user\":\\{\"id\":\"([^\"]+)\".*", "$1");
    }

    private record Credentials(String token, String userId) {
    }
}
