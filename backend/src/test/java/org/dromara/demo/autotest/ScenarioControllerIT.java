package org.dromara.demo.autotest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 自动化测试场景控制器集成测试（H2 + 登录 admin）。
 *
 * @author demo
 * @since 2026-08-15
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScenarioControllerIT {

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

    private long createScenario(String token) throws Exception {
        String body = "{\"name\":\"登录链路\",\"description\":\"测试\",\"baseUrl\":\"http://localhost:8080\","
                + "\"variables\":{\"username\":\"admin\",\"password\":\"123456\"},"
                + "\"steps\":[{\"name\":\"登录\",\"method\":\"POST\",\"path\":\"/api/auth/login\","
                + "\"headers\":{},\"query\":{},\"body\":{\"username\":\"{{username}}\",\"password\":\"{{password}}\"},"
                + "\"asserts\":[{\"type\":\"STATUS\",\"expected\":\"200\"}],\"extracts\":[]}]}";
        MvcResult r = mvc.perform(post("/api/autotest/scenario").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    @Test
    void createAndGetDetail_shouldReturnSteps() throws Exception {
        String token = login();
        long id = createScenario(token);
        mvc.perform(get("/api/autotest/scenario/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("登录链路"))
                .andExpect(jsonPath("$.data.steps[0].method").value("POST"))
                .andExpect(jsonPath("$.data.steps[0].path").value("/api/auth/login"));
    }

    @Test
    void page_shouldReturnList() throws Exception {
        String token = login();
        createScenario(token);
        mvc.perform(get("/api/autotest/scenario/page").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void update_shouldReplaceSteps() throws Exception {
        String token = login();
        long id = createScenario(token);
        String body = "{\"name\":\"登录链路v2\",\"variables\":{},\"steps\":["
                + "{\"name\":\"登出\",\"method\":\"POST\",\"path\":\"/api/auth/logout\",\"headers\":{},\"query\":{},"
                + "\"body\":{},\"asserts\":[],\"extracts\":[]}]}";
        mvc.perform(put("/api/autotest/scenario/" + id).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(get("/api/autotest/scenario/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("登录链路v2"))
                .andExpect(jsonPath("$.data.steps[0].name").value("登出"))
                .andExpect(jsonPath("$.data.steps.length()").value(1));
    }

    @Test
    void delete_shouldRemoveScenario() throws Exception {
        String token = login();
        long id = createScenario(token);
        mvc.perform(delete("/api/autotest/scenario/" + id).header("Authorization", token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/autotest/scenario/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_withBlankName_shouldReturn400() throws Exception {
        String token = login();
        String body = "{\"name\":\"\",\"variables\":{},\"steps\":[{\"name\":\"s\",\"method\":\"POST\",\"path\":\"/x\","
                + "\"headers\":{},\"query\":{},\"body\":{},\"asserts\":[],\"extracts\":[]}]}";
        mvc.perform(post("/api/autotest/scenario").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
