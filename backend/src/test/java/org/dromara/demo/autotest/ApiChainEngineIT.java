package org.dromara.demo.autotest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口链路执行引擎集成测试（RANDOM_PORT + 真实 HTTP 回环）。
 *
 * @author demo
 * @since 2026-08-15
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:autotest_engine;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiChainEngineIT {

    @LocalServerPort
    int port;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("data").get("token").asText();
    }

    private long createScenario(String token, String json) throws Exception {
        MvcResult r = mvc.perform(post("/api/autotest/scenario").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        return om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    private long run(String token, long scenarioId) throws Exception {
        MvcResult r = mvc.perform(post("/api/autotest/scenario/" + scenarioId + "/run")
                .header("Authorization", token))
                .andExpect(status().isOk()).andReturn();
        return om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    private String waitForRun(long runId, String token) throws Exception {
        for (int i = 0; i < 100; i++) {
            MvcResult r = mvc.perform(get("/api/autotest/run/" + runId).header("Authorization", token)).andReturn();
            String body = r.getResponse().getContentAsString();
            if (om.readTree(body).get("data").get("status").asInt() != 0) {
                return body;
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("运行超时未结束");
    }

    @Test
    void run_successChain_shouldPassAllSteps() throws Exception {
        String token = login();
        long scenarioId = createScenario(token, "{\"name\":\"登录-业务-登出\",\"baseUrl\":\"http://localhost:"
                + port + "\",\"variables\":{\"username\":\"admin\",\"password\":\"123456\"},\"steps\":["
                + "{\"name\":\"登录\",\"method\":\"POST\",\"path\":\"/api/auth/login\","
                + "\"headers\":{\"Content-Type\":\"application/json\"},\"query\":{},"
                + "\"body\":{\"username\":\"{{username}}\",\"password\":\"{{password}}\"},"
                + "\"asserts\":[{\"type\":\"STATUS\",\"expected\":\"200\"},"
                + "{\"type\":\"JSON\",\"jsonPath\":\"$.code\",\"op\":\"EQUALS\",\"expected\":\"200\"}],"
                + "\"extracts\":[{\"name\":\"token\",\"jsonPath\":\"$.data.token\"}]},"
                + "{\"name\":\"业务\",\"method\":\"GET\",\"path\":\"/api/project/page\","
                + "\"headers\":{\"Authorization\":\"{{token}}\"},\"query\":{},\"body\":{},"
                + "\"asserts\":[{\"type\":\"JSON\",\"jsonPath\":\"$.code\",\"op\":\"EQUALS\",\"expected\":\"200\"}],"
                + "\"extracts\":[]},"
                + "{\"name\":\"登出\",\"method\":\"POST\",\"path\":\"/api/auth/logout\","
                + "\"headers\":{\"Authorization\":\"{{token}}\"},\"query\":{},\"body\":{},"
                + "\"asserts\":[{\"type\":\"STATUS\",\"expected\":\"200\"}],\"extracts\":[]}]}");
        long runId = run(token, scenarioId);
        JsonNode data = om.readTree(waitForRun(runId, token)).get("data");
        assertEquals(1, data.get("status").asInt());
        assertEquals(3, data.get("steps").size());
        for (JsonNode step : data.get("steps")) {
            assertEquals(0, step.get("status").asInt());
        }
    }

    @Test
    void run_assertFail_shouldAbortAndSkipRemaining() throws Exception {
        String token = login();
        long scenarioId = createScenario(token, "{\"name\":\"失败链路\",\"baseUrl\":\"http://localhost:"
                + port + "\",\"variables\":{\"username\":\"admin\",\"password\":\"123456\"},\"steps\":["
                + "{\"name\":\"登录\",\"method\":\"POST\",\"path\":\"/api/auth/login\","
                + "\"headers\":{},\"query\":{},\"body\":{\"username\":\"{{username}}\",\"password\":\"{{password}}\"},"
                + "\"asserts\":[{\"type\":\"STATUS\",\"expected\":\"200\"}],\"extracts\":[]},"
                + "{\"name\":\"失败业务\",\"method\":\"GET\",\"path\":\"/api/project/page\","
                + "\"headers\":{},\"query\":{},\"body\":{},"
                + "\"asserts\":[{\"type\":\"JSON\",\"jsonPath\":\"$.code\",\"op\":\"EQUALS\",\"expected\":\"999\"}],"
                + "\"extracts\":[]},"
                + "{\"name\":\"不应执行\",\"method\":\"GET\",\"path\":\"/api/project/page\","
                + "\"headers\":{},\"query\":{},\"body\":{},"
                + "\"asserts\":[{\"type\":\"STATUS\",\"expected\":\"200\"}],\"extracts\":[]}]}");
        long runId = run(token, scenarioId);
        JsonNode data = om.readTree(waitForRun(runId, token)).get("data");
        assertEquals(2, data.get("status").asInt());
        JsonNode steps = data.get("steps");
        assertEquals(0, steps.get(0).get("status").asInt());
        assertEquals(1, steps.get(1).get("status").asInt());
        assertEquals(2, steps.get(2).get("status").asInt());
    }
}
