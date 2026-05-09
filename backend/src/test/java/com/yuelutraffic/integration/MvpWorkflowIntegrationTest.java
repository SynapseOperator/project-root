package com.yuelutraffic.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class MvpWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void completeMvpWorkflowKeepsPrivateFieldsHidden() throws Exception {
        Credentials reporter = login("20264001");
        Credentials voter = login("20264002");
        Credentials admin = login("ADMIN-DEMO");

        String reportId = extract(mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + reporter.token())
                        .contentType("application/json")
                        .content("""
                                {
                                  "type":"CONGESTION",
                                  "latitude":28.1703,
                                  "longitude":112.9388,
                                  "locationLabel":"Lushan South Road",
                                  "description":"Northbound slow traffic",
                                  "initialCredibility":50
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("20264001"))))
                .andReturn(), "id");

        mockMvc.perform(post("/api/v1/reports/" + reportId + "/feedback")
                        .header("Authorization", "Bearer " + voter.token())
                        .contentType("application/json")
                        .content("{\"feedbackType\":\"CONFIRM_VALID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidenceScore").isNumber());

        String accidentId = extract(mockMvc.perform(post("/api/v1/accidents")
                        .header("Authorization", "Bearer " + reporter.token())
                        .contentType("application/json")
                        .content("""
                                {
                                  "latitude":28.1703,
                                  "longitude":112.9388,
                                  "locationLabel":"Lushan South Road",
                                  "description":"Minor incident"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("reporter-phone"))))
                .andReturn(), "id");

        String exchangeId = extract(mockMvc.perform(post("/api/v1/accidents/" + accidentId + "/contact-requests")
                        .header("Authorization", "Bearer " + reporter.token())
                        .contentType("application/json")
                        .content("{\"contactType\":\"PHONE\",\"contactValue\":\"reporter-phone\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("reporter-phone"))))
                .andReturn(), "id");

        mockMvc.perform(post("/api/v1/contact-requests/" + exchangeId + "/confirm")
                        .header("Authorization", "Bearer " + voter.token())
                        .contentType("application/json")
                        .content("{\"contactType\":\"WECHAT\",\"contactValue\":\"voter-wechat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibleContacts", hasSize(2)))
                .andExpect(content().string(containsString("reporter-phone")))
                .andExpect(content().string(containsString("voter-wechat")));

        String banUntil = Instant.now().plusSeconds(3600).toString();
        mockMvc.perform(post("/api/v1/admin/users/" + reporter.userId() + "/restrictions")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType("application/json")
                        .content("{\"postingBanUntil\":\"" + banUntil + "\",\"reason\":\"Integration test posting restriction\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + reporter.token())
                        .contentType("application/json")
                        .content("""
                                {
                                  "type":"CONSTRUCTION",
                                  "latitude":28.1703,
                                  "longitude":112.9388,
                                  "description":"Blocked by posting restriction",
                                  "initialCredibility":50
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRequestsReturnClientErrors() throws Exception {
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer malformed.token"))
                .andExpect(status().isUnauthorized());

        Credentials user = login("20264003");
        mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType("application/json")
                        .content("{\"type\":\"UNKNOWN\",\"latitude\":28.1703,\"longitude\":112.9388}"))
                .andExpect(status().isBadRequest());
    }

    private Credentials login(String studentNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"" + studentNumber + "\",\"privacyAcknowledged\":true}"))
                .andExpect(status().isOk())
                .andReturn();
        return new Credentials(extract(result, "accessToken"), extractNestedUserId(result));
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
