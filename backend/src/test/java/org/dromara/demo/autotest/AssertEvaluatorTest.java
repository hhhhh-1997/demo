package org.dromara.demo.autotest;

import org.dromara.demo.autotest.domain.AssertItem;
import org.dromara.demo.autotest.engine.AssertEvaluator;
import org.dromara.demo.autotest.engine.AssertFailedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 断言执行单元测试。
 *
 * @author demo
 * @since 2026-08-15
 */
class AssertEvaluatorTest {

    private final AssertEvaluator evaluator = new AssertEvaluator();

    private AssertItem status(String expected) {
        AssertItem a = new AssertItem();
        a.setType("STATUS");
        a.setExpected(expected);
        return a;
    }

    private AssertItem json(String jsonPath, String op, String expected) {
        AssertItem a = new AssertItem();
        a.setType("JSON");
        a.setJsonPath(jsonPath);
        a.setOp(op);
        a.setExpected(expected);
        return a;
    }

    @Test
    void status_shouldPassWhenMatch() {
        assertDoesNotThrow(() -> evaluator.evaluate(200, "{}", List.of(status("200"))));
    }

    @Test
    void status_shouldFailWhenMismatch() {
        assertThrows(AssertFailedException.class, () -> evaluator.evaluate(200, "{}", List.of(status("500"))));
    }

    @Test
    void jsonEquals_shouldPass() {
        assertDoesNotThrow(() -> evaluator.evaluate(200, "{\"code\":200}", List.of(json("$.code", "EQUALS", "200"))));
    }

    @Test
    void jsonEquals_shouldFail() {
        assertThrows(AssertFailedException.class,
                () -> evaluator.evaluate(200, "{\"code\":500}", List.of(json("$.code", "EQUALS", "200"))));
    }

    @Test
    void jsonContains_shouldPass() {
        assertDoesNotThrow(() ->
                evaluator.evaluate(200, "{\"msg\":\"操作成功\"}", List.of(json("$.msg", "CONTAINS", "成功"))));
    }

    @Test
    void jsonExists_shouldPassWhenPresent() {
        assertDoesNotThrow(() ->
                evaluator.evaluate(200, "{\"data\":{\"token\":\"x\"}}", List.of(json("$.data.token", "EXISTS", null))));
    }

    @Test
    void jsonExists_shouldFailWhenAbsent() {
        assertThrows(AssertFailedException.class,
                () -> evaluator.evaluate(200, "{\"data\":{}}", List.of(json("$.data.token", "EXISTS", null))));
    }

    @Test
    void evaluate_success_shouldReturnDetailLines() {
        String detail = evaluator.evaluate(200, "{\"code\":200}",
                List.of(status("200"), json("$.code", "EQUALS", "200")));
        assertTrue(detail.contains("STATUS"));
        assertTrue(detail.contains("JSON"));
    }
}
