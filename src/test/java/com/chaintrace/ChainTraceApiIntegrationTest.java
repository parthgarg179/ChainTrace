package com.chaintrace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests through the real HTTP layer, against the BMW India mock database.
 * The tests also load a second, test-only company (src/test/resources/companies)
 * to prove that one company's login token is rejected on another company's key.
 */
@SpringBootTest(properties = "chaintrace.companies-location=classpath*:companies/*.yml")
@AutoConfigureMockMvc
class ChainTraceApiIntegrationTest {

    private static final String BMW_KEY = "bmw-demo-key-2b81e4";
    private static final String SECOND_COMPANY_KEY = "second-company-test-key";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String login(String apiKey, String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .header("X-ChainTrace-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"demo123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        return node.get("token").asText();
    }

    @Test
    void carHistoryHasThreeOwnersInOrder() throws Exception {
        String token = login(BMW_KEY, "karan@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA5R1C0XK8B45210/history")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.product.model").value("BMW 3 Series Gran Limousine"))
                .andExpect(jsonPath("$.product.warrantyStatus").value("EXPIRED"))
                .andExpect(jsonPath("$.owners", hasSize(3)))
                .andExpect(jsonPath("$.owners[0].ownerName").value("Meera Joshi"))
                .andExpect(jsonPath("$.currentOwner").value("Karan Singh"))
                .andExpect(jsonPath("$.previousOwnerCount").value(2));
    }

    @Test
    void accidentAlertAndExtraColumnsArePassedThrough() throws Exception {
        String token = login(BMW_KEY, "karan@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA11AA0XP5Y78830/history")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLabel").value("VIN"))
                .andExpect(jsonPath("$.product.alert").value("Accident reported"))
                .andExpect(jsonPath("$.owners", hasSize(2)))
                .andExpect(jsonPath("$.owners[0].channel").value("Dealer sale"))
                .andExpect(jsonPath("$.owners[1].details.odometer_km").value(24300));
    }

    @Test
    void openRecallCampaignIsShownAsAlert() throws Exception {
        String token = login(BMW_KEY, "karan@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA31EF06S8P01200/history")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.model").value("BMW iX1 LWB"))
                .andExpect(jsonPath("$.product.alert").value(
                        org.hamcrest.Matchers.startsWith("Open recall campaign:")))
                .andExpect(jsonPath("$.owners", hasSize(1)));
    }

    @Test
    void unknownProductReturns404() throws Exception {
        String token = login(BMW_KEY, "vikram@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA99ZZ99S9999999/history")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void invalidVinReturns400() throws Exception {
        String token = login(BMW_KEY, "vikram@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA5R1COXK8B45210/history")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PRODUCT_ID"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"karan@bmw.demo\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void missingLicenceKeyIsRejected() throws Exception {
        mvc.perform(get("/api/v1/company"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_API_KEY"));
    }

    @Test
    void lookupWithoutLoginIsRejected() throws Exception {
        mvc.perform(get("/api/v1/products/WBA5R1C0XK8B45210/history")
                        .header("X-ChainTrace-Key", BMW_KEY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("SESSION_EXPIRED"));
    }

    @Test
    void tokenFromAnotherCompanyIsRejected() throws Exception {
        String otherToken = login(SECOND_COMPANY_KEY, "karan@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA5R1C0XK8B45210/history")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void certificateDownloadsAsCsv() throws Exception {
        String token = login(BMW_KEY, "meera@bmw.demo");

        mvc.perform(get("/api/v1/products/WBA5R1C0XK8B45210/certificate.csv")
                        .header("X-ChainTrace-Key", BMW_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("chaintrace-WBA5R1C0XK8B45210.csv")));
    }
}
