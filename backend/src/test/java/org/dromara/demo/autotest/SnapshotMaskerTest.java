package org.dromara.demo.autotest;

import org.dromara.demo.autotest.engine.SnapshotMasker;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 快照脱敏单元测试。
 *
 * @author demo
 * @since 2026-08-15
 */
class SnapshotMaskerTest {

    private final SnapshotMasker masker = new SnapshotMasker();

    @Test
    void mask_shouldMaskPasswordKey() {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("password", "secret");
        assertEquals("******", masker.mask(snap).get("password"));
    }

    @Test
    void mask_shouldMaskAuthorizationHeader() {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer abc");
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("headers", headers);
        Map<?, ?> maskedHeaders = (Map<?, ?>) masker.mask(snap).get("headers");
        assertEquals("******", maskedHeaders.get("Authorization"));
    }

    @Test
    void mask_shouldMaskNestedTokenInBody() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", "abc123");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", data);
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("body", body);
        Map<?, ?> maskedData = (Map<?, ?>) ((Map<?, ?>) masker.mask(snap).get("body")).get("data");
        assertEquals("******", maskedData.get("token"));
    }

    @Test
    void mask_shouldLeaveOtherKeys() {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("name", "场景A");
        assertEquals("场景A", masker.mask(snap).get("name"));
    }
}
