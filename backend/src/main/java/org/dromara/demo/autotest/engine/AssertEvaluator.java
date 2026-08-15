package org.dromara.demo.autotest.engine;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.dromara.demo.autotest.domain.AssertItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 断言执行：STATUS（状态码）与 JSON（EQUALS/CONTAINS/EXISTS）。
 *
 * @author demo
 * @since 2026-08-15
 */
@Component
public class AssertEvaluator {

    public String evaluate(int statusCode, String responseBody, List<AssertItem> asserts) {
        List<String> lines = new ArrayList<>();
        if (asserts == null) {
            return "";
        }
        for (AssertItem item : asserts) {
            if ("STATUS".equalsIgnoreCase(item.getType())) {
                evaluateStatus(statusCode, item, lines);
            } else if ("JSON".equalsIgnoreCase(item.getType())) {
                evaluateJson(responseBody, item, lines);
            } else {
                throw new AssertFailedException(join(lines), "未知断言类型: " + item.getType());
            }
        }
        return String.join("\n", lines);
    }

    private void evaluateStatus(int statusCode, AssertItem item, List<String> lines) {
        String actual = String.valueOf(statusCode);
        String expected = item.getExpected();
        if (expected == null) {
            throw new AssertFailedException(join(lines), "STATUS 断言缺少期望值");
        }
        if (actual.equals(expected)) {
            lines.add("STATUS: expected=" + expected + " actual=" + actual + " PASS");
        } else {
            throw new AssertFailedException(
                    appendFail(lines, "STATUS: expected=" + expected + " actual=" + actual + " FAIL"),
                    "状态码不符：期望 " + expected + "，实际 " + actual);
        }
    }

    private void evaluateJson(String responseBody, AssertItem item, List<String> lines) {
        String path = item.getJsonPath();
        String op = item.getOp() == null ? "" : item.getOp().toUpperCase();
        if (path == null) {
            throw new AssertFailedException(join(lines), "JSON 断言缺少 jsonPath");
        }
        Object actual;
        try {
            actual = JsonPath.read(responseBody, path);
        } catch (PathNotFoundException e) {
            throw new AssertFailedException(
                    appendFail(lines, "JSON " + op + " " + path + ": 路径不存在 FAIL"),
                    "JSONPath 无匹配: " + path);
        }
        switch (op) {
            case "EXISTS" -> {
                if (actual != null) {
                    lines.add("JSON EXISTS " + path + " PASS");
                } else {
                    throw new AssertFailedException(
                            appendFail(lines, "JSON EXISTS " + path + ": 值为 null FAIL"),
                            "JSONPath 值为空: " + path);
                }
            }
            case "EQUALS" -> {
                String actualStr = String.valueOf(actual);
                if (actualStr.equals(item.getExpected())) {
                    lines.add("JSON EQUALS " + path + ": expected=" + item.getExpected()
                            + " actual=" + actualStr + " PASS");
                } else {
                    throw new AssertFailedException(
                            appendFail(lines, "JSON EQUALS " + path + ": expected=" + item.getExpected()
                                    + " actual=" + actualStr + " FAIL"),
                            "字段值不符: " + path + " 期望 " + item.getExpected() + " 实际 " + actualStr);
                }
            }
            case "CONTAINS" -> {
                String actualStr = String.valueOf(actual);
                String expected = item.getExpected() == null ? "" : item.getExpected();
                if (actualStr.contains(expected)) {
                    lines.add("JSON CONTAINS " + path + ": PASS");
                } else {
                    throw new AssertFailedException(
                            appendFail(lines, "JSON CONTAINS " + path + ": FAIL"),
                            "字段值不包含: " + path);
                }
            }
            default -> throw new AssertFailedException(join(lines), "未知断言操作: " + item.getOp());
        }
    }

    private String join(List<String> lines) {
        return String.join("\n", lines);
    }

    private String appendFail(List<String> lines, String failLine) {
        String base = join(lines);
        return base.isEmpty() ? failLine : base + "\n" + failLine;
    }
}
