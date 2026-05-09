package com.yuelutraffic.accidents;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
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
class AccidentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accidentContactIsHiddenUntilMutualConfirmation() throws Exception {
        String alice = login("20262001");
        String bob = login("20262002");
        String outsider = login("20262003");

        MvcResult accident = createAccident(alice)
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("13800000000"))))
                .andReturn();
        String accidentId = extract(accident, "id");

        mockMvc.perform(get("/api/v1/accidents/" + accidentId))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("13800000000"))));

        MvcResult exchange = mockMvc.perform(post("/api/v1/accidents/" + accidentId + "/contact-requests")
                        .header("Authorization", "Bearer " + alice)
                        .contentType("application/json")
                        .content("{\"contactType\":\"PHONE\",\"contactValue\":\"13800000000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.visibleContacts", empty()))
                .andExpect(content().string(not(containsString("13800000000"))))
                .andReturn();
        String requestId = extract(exchange, "id");

        mockMvc.perform(post("/api/v1/contact-requests/" + requestId + "/confirm")
                        .header("Authorization", "Bearer " + bob)
                        .contentType("application/json")
                        .content("{\"contactType\":\"WECHAT\",\"contactValue\":\"bob-wechat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MUTUALLY_CONFIRMED"))
                .andExpect(jsonPath("$.visibleContacts", hasSize(2)))
                .andExpect(content().string(containsString("13800000000")))
                .andExpect(content().string(containsString("bob-wechat")));

        mockMvc.perform(get("/api/v1/contact-requests/" + requestId)
                        .header("Authorization", "Bearer " + outsider))
                .andExpect(status().isForbidden());
    }

    private String login(String studentNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/student")
                        .contentType("application/json")
                        .content("{\"studentNumber\":\"" + studentNumber + "\",\"privacyAcknowledged\":true}"))
                .andExpect(status().isOk())
                .andReturn();
        return extract(result, "accessToken");
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
                          "description":"Minor incident, looking for the other involved party."
                        }
                        """));
    }

    private String extract(MvcResult result, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceFirst(".*?\"" + field + "\":\"([^\"]+)\".*", "$1");
    }
}
