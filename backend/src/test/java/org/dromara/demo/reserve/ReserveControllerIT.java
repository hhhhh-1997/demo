package org.dromara.demo.reserve;

import com.fasterxml.jackson.databind.JsonNode;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 统一储备库控制器集成测试（H2 + 登录 admin）。
 *
 * @author demo
 * @since 2026-08-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReserveControllerIT {

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

    private long createAndReachPendingIssue(String token, String name) throws Exception {
        String createBody = "{\"projectName\":\"" + name + "\",\"projectType\":\"电网基建\",\"investmentAmount\":800000,\"deptId\":1}";
        MvcResult r = mvc.perform(post("/api/project").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isOk()).andReturn();
        long id = om.readTree(r.getResponse().getContentAsString()).get("data").asLong();
        mvc.perform(post("/api/project/submit/" + id).header("Authorization", token))
                .andExpect(status().isOk());
        String reviewBody = "{\"infoComplete\":\"完整\",\"threeImportant\":\"符合\",\"splitProject\":\"无\","
                + "\"interfaceConfusion\":\"无\",\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/review/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(reviewBody))
                .andExpect(status().isOk());
        String auditBody = "{\"opinion\":\"同意\",\"pass\":true}";
        mvc.perform(post("/api/audit/" + id + "/execute").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(auditBody))
                .andExpect(status().isOk());
        return id;
    }

    @Test
    void issue_shouldMoveToIssuedAndSetIssueTime() throws Exception {
        String token = loginAndGetToken();
        long id = createAndReachPendingIssue(token, "储备库下达项目");
        mvc.perform(post("/api/reserve/" + id + "/issue").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        String detail = mvc.perform(get("/api/reserve/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode project = om.readTree(detail).get("data").get("project");
        assertEquals("已下达", project.get("status").asText());
        assertNotNull(project.get("issueTime"));
        assertFalse(project.get("issueTime").isNull());
    }

    @Test
    void issue_againOnIssued_shouldReturn400() throws Exception {
        String token = loginAndGetToken();
        long id = createAndReachPendingIssue(token, "储备库重复下达项目");
        mvc.perform(post("/api/reserve/" + id + "/issue").header("Authorization", token))
                .andExpect(status().isOk());
        mvc.perform(post("/api/reserve/" + id + "/issue").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void stats_shouldReturnTotalAndDistributions() throws Exception {
        String token = loginAndGetToken();
        long id = createAndReachPendingIssue(token, "储备库统计项目");
        mvc.perform(post("/api/reserve/" + id + "/issue").header("Authorization", token))
                .andExpect(status().isOk());
        String body = mvc.perform(get("/api/reserve/stats").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = om.readTree(body).get("data");
        assertTrue(data.get("total").asLong() >= 1);
        assertTrue(data.get("issued").asLong() >= 1);
        assertNotNull(data.get("totalAmountYuan"));
        boolean foundCategory = false;
        for (JsonNode node : data.get("categoryDist")) {
            if ("电网基建".equals(node.get("name").asText()) && node.get("value").asLong() >= 1) {
                foundCategory = true;
            }
        }
        assertTrue(foundCategory, "categoryDist 应包含已下达项目的二级分类");
        boolean foundDept = false;
        for (JsonNode node : data.get("deptDist")) {
            if ("省公司".equals(node.get("name").asText()) && node.get("value").asLong() >= 1) {
                foundDept = true;
            }
        }
        assertTrue(foundDept, "deptDist 应包含已下达项目的所属单位");
    }
}
