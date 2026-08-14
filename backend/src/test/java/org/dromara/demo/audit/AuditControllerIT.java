package org.dromara.demo.audit;

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
 * 项目审核控制器集成测试（H2 + 登录 admin）。
 *
 * @author demo
 * @since 2026-08-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditControllerIT {

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

    private long createProject(String token, String name) throws Exception {
        String createBody = "{\"projectName\":\"" + name + "\",\"projectType\":\"电网基建\",\"investmentAmount\":600000,\"deptId\":2}";
        MvcResult r = mvc.perform(post("/api/project").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isOk()).andReturn();
        return om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
    }

    private long createSubmitAndReviewPass(String token) throws Exception {
        long id = createProject(token, "审核测试项目");
        mvc.perform(post("/api/project/submit/" + id).header("Authorization", token))
                .andExpect(status().isOk());
        String reviewBody = "{\"infoComplete\":\"完整\",\"threeImportant\":\"符合\",\"splitProject\":\"无\","
                + "\"interfaceConfusion\":\"无\",\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/review/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(reviewBody))
                .andExpect(status().isOk());
        return id;
    }

    private String statusOf(String token, long id) throws Exception {
        String detail = mvc.perform(get("/api/project/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(detail).get("data").get("status").asText();
    }

    @Test
    void execute_pass_shouldMoveToPendingIssue() throws Exception {
        String token = loginAndGetToken();
        long id = createSubmitAndReviewPass(token);
        String body = "{\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/audit/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertEquals("待下达", statusOf(token, id));
    }

    @Test
    void execute_fail_shouldMoveToAuditRejected() throws Exception {
        String token = loginAndGetToken();
        long id = createSubmitAndReviewPass(token);
        String body = "{\"opinion\":\"退回修改\",\"pass\":false}";
        mvc.perform(post("/api/audit/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertEquals("审核退回", statusOf(token, id));
    }

    @Test
    void batch_pass_shouldMoveAllToPendingIssue() throws Exception {
        String token = loginAndGetToken();
        long id1 = createSubmitAndReviewPass(token);
        long id2 = createSubmitAndReviewPass(token);
        String body = "{\"ids\":[" + id1 + "," + id2 + "],\"pass\":true}";
        mvc.perform(post("/api/audit/batch").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertEquals("待下达", statusOf(token, id1));
        assertEquals("待下达", statusOf(token, id2));
    }

    @Test
    void execute_onNonPendingAudit_shouldReturn400() throws Exception {
        String token = loginAndGetToken();
        long id = createProject(token, "草稿审核项目");
        String body = "{\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/audit/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
