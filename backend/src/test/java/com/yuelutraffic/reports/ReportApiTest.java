package com.yuelutraffic.reports;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
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
class ReportApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userCanCreateAndListTrafficReportWithoutLeakingStudentNumber() throws Exception {
        String token = login("20261001");
        MvcResult created = createReport(token, "CONGESTION", 28.1703, 112.9388)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("CONGESTION")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.confidenceScore", greaterThan(0)))
                .andExpect(content().string(not(containsString("20261001"))))
                .andReturn();

        String reportId = extract(created, "id");

        mockMvc.perform(get("/api/v1/reports/" + reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultExpiresAt").exists())
                .andExpect(content().string(not(containsString("studentNumberHash"))));

        mockMvc.perform(get("/api/v1/reports")
                        .param("minLat", "28.12")
                        .param("minLng", "112.88")
                        .param("maxLat", "28.21")
                        .param("maxLng", "112.99"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(reportId)));
    }

    @Test
    void feedbackChangesConfidenceAndRejectsDuplicateVotes() throws Exception {
        String submitter = login("20261002");
        String voter = login("20261003");
        String reportId = extract(createReport(submitter, "ROAD_CONTROL", 28.1703, 112.9388)
                .andExpect(status().isOk())
                .andReturn(), "id");

        mockMvc.perform(post("/api/v1/reports/" + reportId + "/feedback")
                        .header("Authorization", "Bearer " + voter)
                        .contentType("application/json")
                        .content("{\"feedbackType\":\"CONFIRM_VALID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidenceScore", greaterThan(55)));

        mockMvc.perform(post("/api/v1/reports/" + reportId + "/feedback")
                        .header("Authorization", "Bearer " + voter)
                        .contentType("application/json")
                        .content("{\"feedbackType\":\"MARK_EXPIRED\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User-")))
                .andExpect(content().string(not(containsString("20261002"))));
    }

    @Test
    void communityExpiredFeedbackCanExpireReport() throws Exception {
        String submitter = login("20261004");
        String voterOne = login("20261005");
        String voterTwo = login("20261006");
        String reportId = extract(createReport(submitter, "CONGESTION", 28.1703, 112.9388)
                .andExpect(status().isOk())
                .andReturn(), "id");

        markExpired(reportId, voterOne).andExpect(status().isOk());
        markExpired(reportId, voterTwo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EXPIRED")));
    }

    @Test
    void reportOutsidePilotAreaIsRejected() throws Exception {
        String token = login("20261007");
        createReport(token, "CONSTRUCTION", 27.9, 112.7)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("outside")));
    }

    private String login(String studentNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"" + studentNumber + "\",\"privacyAcknowledged\":true}"))
                .andExpect(status().isOk())
                .andReturn();
        return extract(result, "accessToken");
    }

    private org.springframework.test.web.servlet.ResultActions createReport(String token, String type, double latitude, double longitude) throws Exception {
        return mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                          "type":"%s",
                          "latitude":%s,
                          "longitude":%s,
                          "locationLabel":"Lushan South Road",
                          "description":"Local road condition report",
                          "initialCredibility":50
                        }
                        """.formatted(type, latitude, longitude)));
    }

    private org.springframework.test.web.servlet.ResultActions markExpired(String reportId, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/reports/" + reportId + "/feedback")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"feedbackType\":\"MARK_EXPIRED\"}"));
    }

    private String extract(MvcResult result, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceFirst(".*?\"" + field + "\":\"([^\"]+)\".*", "$1");
    }
}
