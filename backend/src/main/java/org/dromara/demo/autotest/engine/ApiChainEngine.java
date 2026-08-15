package org.dromara.demo.autotest.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.dromara.demo.autotest.domain.AtRun;
import org.dromara.demo.autotest.domain.AtScenario;
import org.dromara.demo.autotest.domain.AtStep;
import org.dromara.demo.autotest.domain.AtStepResult;
import org.dromara.demo.autotest.domain.ExtractItem;
import org.dromara.demo.autotest.domain.RunStatus;
import org.dromara.demo.autotest.domain.StepParams;
import org.dromara.demo.autotest.domain.StepStatus;
import org.dromara.demo.autotest.mapper.AtRunMapper;
import org.dromara.demo.autotest.mapper.AtScenarioMapper;
import org.dromara.demo.autotest.mapper.AtStepMapper;
import org.dromara.demo.autotest.mapper.AtStepResultMapper;
import org.dromara.demo.common.BusinessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 接口链路执行引擎：顺序执行、失败中止、变量传递、断言、脱敏落库。
 *
 * @author demo
 * @since 2026-08-15
 */
@Service
public class ApiChainEngine {

    private final AtScenarioMapper scenarioMapper;
    private final AtStepMapper stepMapper;
    private final AtRunMapper runMapper;
    private final AtStepResultMapper stepResultMapper;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final ThreadPoolExecutor executor;
    private final PlaceholderResolver placeholderResolver;
    private final AssertEvaluator assertEvaluator;
    private final SnapshotMasker snapshotMasker;

    public ApiChainEngine(AtScenarioMapper scenarioMapper, AtStepMapper stepMapper, AtRunMapper runMapper,
            AtStepResultMapper stepResultMapper, ObjectMapper objectMapper, RestClient restClient,
            ThreadPoolExecutor executor, PlaceholderResolver placeholderResolver, AssertEvaluator assertEvaluator,
            SnapshotMasker snapshotMasker) {
        this.scenarioMapper = scenarioMapper;
        this.stepMapper = stepMapper;
        this.runMapper = runMapper;
        this.stepResultMapper = stepResultMapper;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
        this.executor = executor;
        this.placeholderResolver = placeholderResolver;
        this.assertEvaluator = assertEvaluator;
        this.snapshotMasker = snapshotMasker;
    }

    public Long run(Long scenarioId, Long triggerBy) {
        AtScenario scenario = scenarioMapper.selectById(scenarioId);
        if (scenario == null) {
            throw new BusinessException("场景不存在");
        }
        List<AtStep> steps = stepMapper.selectList(new LambdaQueryWrapper<AtStep>()
                .eq(AtStep::getScenarioId, scenarioId)
                .orderByAsc(AtStep::getStepOrder));
        AtRun run = new AtRun();
        run.setScenarioId(scenarioId);
        run.setStatus(RunStatus.RUNNING.getCode());
        run.setStartTime(LocalDateTime.now());
        run.setCreateTime(LocalDateTime.now());
        run.setTriggerBy(triggerBy);
        runMapper.insert(run);
        executor.execute(() -> execute(run.getId(), scenario, steps));
        return run.getId();
    }

    private void execute(Long runId, AtScenario scenario, List<AtStep> steps) {
        try {
            Map<String, Object> ctx = parseVariables(scenario.getVariables());
            for (int i = 0; i < steps.size(); i++) {
                AtStepResult result = executeStep(runId, scenario, steps.get(i), ctx);
                if (!Integer.valueOf(StepStatus.PASS.getCode()).equals(result.getStatus())) {
                    for (int j = i + 1; j < steps.size(); j++) {
                        insertSkipped(runId, steps.get(j));
                    }
                    failRun(runId, steps.get(i).getId(), result.getErrorMsg());
                    return;
                }
            }
            successRun(runId);
        } catch (Exception e) {
            failRun(runId, null, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private AtStepResult executeStep(Long runId, AtScenario scenario, AtStep step, Map<String, Object> ctx) {
        AtStepResult result = new AtStepResult();
        result.setRunId(runId);
        result.setStepId(step.getId());
        result.setStepOrder(step.getStepOrder());
        result.setName(step.getName());
        result.setStartTime(LocalDateTime.now());
        try {
            StepParams params = objectMapper.readValue(step.getParams(), StepParams.class);
            Map<String, String> headers = placeholderResolver.resolveMap(params.getHeaders(), ctx);
            Map<String, String> query = placeholderResolver.resolveMap(params.getQuery(), ctx);
            String bodyStr = params.getBody() == null ? null
                    : placeholderResolver.resolve(objectMapper.writeValueAsString(params.getBody()), ctx);
            String url = buildUrl(scenario.getBaseUrl(), params.getPath());
            ResponseEntity<String> response = send(HttpMethod.valueOf(params.getMethod().toUpperCase()),
                    url, query, headers, bodyStr);
            result.setRequestSnapshot(objectMapper.writeValueAsString(
                    snapshotMasker.mask(buildRequestSnapshot(params.getMethod(), params.getPath(),
                            headers, query, params.getBody()))));
            result.setResponseSnapshot(objectMapper.writeValueAsString(
                    snapshotMasker.mask(buildResponseSnapshot(response))));
            String detail = assertEvaluator.evaluate(response.getStatusCode().value(), response.getBody(),
                    params.getAsserts());
            result.setAssertDetail(detail);
            applyExtracts(params.getExtracts(), response.getBody(), ctx);
            result.setStatus(StepStatus.PASS.getCode());
        } catch (AssertFailedException e) {
            result.setStatus(StepStatus.FAIL.getCode());
            result.setAssertDetail(e.getDetail());
            result.setErrorMsg(e.getMessage());
        } catch (BusinessException e) {
            result.setStatus(StepStatus.FAIL.getCode());
            result.setErrorMsg(e.getMessage());
        } catch (Exception e) {
            result.setStatus(StepStatus.FAIL.getCode());
            result.setErrorMsg(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            result.setEndTime(LocalDateTime.now());
            stepResultMapper.insert(result);
        }
        return result;
    }

    private ResponseEntity<String> send(HttpMethod method, String url, Map<String, String> query,
            Map<String, String> headers, String body) {
        String target = url;
        if (query != null && !query.isEmpty()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            query.forEach(builder::queryParam);
            target = builder.build().encode().toUriString();
        }
        RestClient.RequestBodySpec spec = restClient.method(method)
                .uri(target)
                .headers(h -> {
                    if (headers != null) {
                        headers.forEach(h::set);
                    }
                });
        if (body != null) {
            spec = spec.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return spec.retrieve().toEntity(String.class);
    }

    private String buildUrl(String baseUrl, String path) {
        String base = (baseUrl == null || baseUrl.isBlank()) ? "http://localhost:8080" : baseUrl;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String p = path.startsWith("/") ? path : "/" + path;
        return base + p;
    }

    private Map<String, Object> buildRequestSnapshot(String method, String path, Map<String, String> headers,
            Map<String, String> query, Map<String, Object> body) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("method", method);
        snap.put("path", path);
        snap.put("headers", headers);
        snap.put("query", query);
        snap.put("body", body);
        return snap;
    }

    private Map<String, Object> buildResponseSnapshot(ResponseEntity<String> response) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("status", response.getStatusCode().value());
        snap.put("headers", response.getHeaders().toSingleValueMap());
        Object body = response.getBody();
        if (body != null) {
            try {
                body = objectMapper.readValue(response.getBody(), Object.class);
            } catch (JsonProcessingException e) {
                // 非 JSON 响应，保留原文
            }
        }
        snap.put("body", body);
        return snap;
    }

    private void applyExtracts(List<ExtractItem> extracts, String responseBody, Map<String, Object> ctx) {
        if (extracts == null) {
            return;
        }
        for (ExtractItem ex : extracts) {
            Object value = JsonPath.read(responseBody, ex.getJsonPath());
            ctx.put(ex.getName(), value);
        }
    }

    private void insertSkipped(Long runId, AtStep step) {
        AtStepResult r = new AtStepResult();
        r.setRunId(runId);
        r.setStepId(step.getId());
        r.setStepOrder(step.getStepOrder());
        r.setName(step.getName());
        r.setStatus(StepStatus.SKIP.getCode());
        r.setErrorMsg("前序步骤失败，跳过");
        stepResultMapper.insert(r);
    }

    private void failRun(Long runId, Long failStepId, String errorMsg) {
        AtRun run = runMapper.selectById(runId);
        run.setStatus(RunStatus.FAILED.getCode());
        run.setFailStepId(failStepId);
        run.setErrorMsg(errorMsg);
        run.setEndTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    private void successRun(Long runId) {
        AtRun run = runMapper.selectById(runId);
        run.setStatus(RunStatus.SUCCESS.getCode());
        run.setEndTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    private Map<String, Object> parseVariables(String variablesJson) {
        if (variablesJson == null || variablesJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(variablesJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("场景预置变量格式错误");
        }
    }
}
