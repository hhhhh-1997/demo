package org.dromara.demo.autotest;

import org.dromara.demo.autotest.engine.PlaceholderResolver;
import org.dromara.demo.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 变量替换单元测试。
 *
 * @author demo
 * @since 2026-08-15
 */
class PlaceholderResolverTest {

    private final PlaceholderResolver resolver = new PlaceholderResolver();

    @Test
    void resolve_shouldReplaceSinglePlaceholder() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("username", "admin");
        assertEquals("admin", resolver.resolve("{{username}}", ctx));
    }

    @Test
    void resolve_shouldReplaceMultiplePlaceholders() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("a", "1");
        ctx.put("b", "2");
        assertEquals("1-2", resolver.resolve("{{a}}-{{b}}", ctx));
    }

    @Test
    void resolve_shouldReplacePlaceholderInsideText() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("token", "abc");
        assertEquals("Bearer abc", resolver.resolve("Bearer {{token}}", ctx));
    }

    @Test
    void resolve_shouldThrowWhenVariableMissing() {
        assertThrows(BusinessException.class, () -> resolver.resolve("{{nope}}", new HashMap<>()));
    }

    @Test
    void resolveMap_shouldResolveAllValues() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("token", "abc");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "{{token}}");
        assertEquals("abc", resolver.resolveMap(headers, ctx).get("Authorization"));
    }
}
