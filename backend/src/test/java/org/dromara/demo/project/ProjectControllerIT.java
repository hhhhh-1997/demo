package org.dromara.demo.project;

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
 * 项目维护控制器集成测试（H2 + 登录 admin）。
 *
 * @author demo
 * @since 2026-08-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerIT {

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

    @Test
    void create_then_submit_shouldBePendingReview() throws Exception {
        String token = loginAndGetToken();
        // POST 新增（金额 500000 元 = 50 万元）
        String createBody = "{\"projectName\":\"测试项目\",\"projectType\":\"电网基建\",\"investmentAmount\":500000,\"deptId\":2}";
        MvcResult r = mvc.perform(post("/api/project").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isOk()).andReturn();
        long id = om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
        // 提报
        mvc.perform(post("/api/project/submit/" + id).header("Authorization", token))
                .andExpect(status().isOk());
        // 断言状态待论证
        String detail = mvc.perform(get("/api/project/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("待论证", om.readTree(detail).get("data").get("status").asText());
    }

    @Test
    void create_shouldRejectInvalidAmount() throws Exception {
        String token = loginAndGetToken();
        String body = "{\"projectName\":\"x\",\"projectType\":\"电网基建\",\"investmentAmount\":0,\"deptId\":2}";
        mvc.perform(post("/api/project").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void access_withoutLogin_shouldReturn401() throws Exception {
        mvc.perform(get("/api/project/page"))
                .andExpect(jsonPath("$.code").value(401));
    }
}
