package org.dromara.demo.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 项目论证控制器集成测试（H2 + 登录 admin）。
 *
 * @author demo
 * @since 2026-08-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    private String loginAndGetToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("data").get("token").asText();
    }

    private long createAndSubmit(String token) throws Exception {
        String createBody = "{\"projectName\":\"论证测试项目\",\"projectType\":\"电网基建\",\"investmentAmount\":600000,\"deptId\":2}";
        MvcResult r = mvc.perform(post("/api/project").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isOk()).andReturn();
        long id = om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
        mvc.perform(post("/api/project/submit/" + id).header("Authorization", token))
                .andExpect(status().isOk());
        return id;
    }

    @Test
    void execute_pass_shouldMoveToPendingAudit() throws Exception {
        String token = loginAndGetToken();
        long id = createAndSubmit(token);
        String body = "{\"infoComplete\":\"完整\",\"threeImportant\":\"符合\",\"splitProject\":\"无\","
                + "\"interfaceConfusion\":\"无\",\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/review/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        String detail = mvc.perform(get("/api/project/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("待审核", om.readTree(detail).get("data").get("status").asText());
    }

    @Test
    void execute_fail_shouldMoveToReviewRejected() throws Exception {
        String token = loginAndGetToken();
        long id = createAndSubmit(token);
        String body = "{\"infoComplete\":\"不完整\",\"threeImportant\":\"不符合\",\"splitProject\":\"存在\","
                + "\"interfaceConfusion\":\"存在\",\"opinion\":\"需补充\",\"pass\":false}";
        mvc.perform(post("/api/review/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        String detail = mvc.perform(get("/api/project/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("论证退回", om.readTree(detail).get("data").get("status").asText());
    }

    @Test
    void execute_onDraft_shouldReturn400() throws Exception {
        String token = loginAndGetToken();
        String createBody = "{\"projectName\":\"草稿项目\",\"projectType\":\"电网基建\",\"investmentAmount\":700000,\"deptId\":2}";
        MvcResult r = mvc.perform(post("/api/project").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isOk()).andReturn();
        long id = om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
        String body = "{\"infoComplete\":\"完整\",\"threeImportant\":\"符合\",\"splitProject\":\"无\","
                + "\"interfaceConfusion\":\"无\",\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/review/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void detail_shouldReturnLatestReviewRecord() throws Exception {
        String token = loginAndGetToken();
        long id = createAndSubmit(token);
        String body = "{\"infoComplete\":\"完整\",\"threeImportant\":\"符合\",\"splitProject\":\"无\","
                + "\"interfaceConfusion\":\"无\",\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/review/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(get("/api/review/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review.result").value("通过"))
                .andExpect(jsonPath("$.data.review.infoComplete").value("完整"))
                .andExpect(jsonPath("$.data.project.id").value(id));
    }
}
