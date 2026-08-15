package org.dromara.demo.autotest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.demo.autotest.domain.AtRun;
import org.dromara.demo.autotest.domain.AtStepResult;
import org.dromara.demo.autotest.mapper.AtRunMapper;
import org.dromara.demo.autotest.mapper.AtStepResultMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 运行历史控制器集成测试（直接落库种子数据）。
 *
 * @author demo
 * @since 2026-08-15
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RunControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Autowired
    AtRunMapper runMapper;

    @Autowired
    AtStepResultMapper stepResultMapper;

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("data").get("token").asText();
    }

    @Test
    void detail_shouldReturnRunAndSteps() throws Exception {
        String token = login();
        AtRun run = new AtRun();
        run.setScenarioId(1L);
        run.setStatus(2);
        run.setErrorMsg("断言失败");
        run.setStartTime(LocalDateTime.now());
        run.setEndTime(LocalDateTime.now());
        run.setCreateTime(LocalDateTime.now());
        runMapper.insert(run);

        AtStepResult r = new AtStepResult();
        r.setRunId(run.getId());
        r.setStepId(1L);
        r.setStepOrder(1);
        r.setName("登录");
        r.setStatus(1);
        r.setRequestSnapshot("{\"method\":\"POST\"}");
        r.setResponseSnapshot("{\"status\":200}");
        r.setAssertDetail("STATUS: expected=200 actual=200 PASS");
        r.setErrorMsg("断言失败");
        stepResultMapper.insert(r);

        mvc.perform(get("/api/autotest/run/" + run.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.steps[0].name").value("登录"))
                .andExpect(jsonPath("$.data.steps[0].status").value(1));
    }

    @Test
    void page_shouldReturnList() throws Exception {
        String token = login();
        mvc.perform(get("/api/autotest/run/page").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }
}
