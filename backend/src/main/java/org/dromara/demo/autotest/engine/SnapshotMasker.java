package org.dromara.demo.autotest.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 请求/响应快照脱敏。
 *
 * @author demo
 * @since 2026-08-15
 */
@Component
public class SnapshotMasker {

    private static final Set<String> SENSITIVE_KEYS = Set.of("password", "pwd", "authorization", "token");

    public Map<String, Object> mask(Map<String, Object> snapshot) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : snapshot.entrySet()) {
            if (isSensitive(e.getKey())) {
                result.put(e.getKey(), "******");
            } else {
                result.put(e.getKey(), maskValue(e.getValue()));
            }
        }
        return result;
    }

    private Object maskValue(Object value) {
        if (value instanceof Map<?, ?> m) {
            Map<Object, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof String key && isSensitive(key)) {
                    out.put(e.getKey(), "******");
                } else {
                    out.put(e.getKey(), maskValue(e.getValue()));
                }
            }
            return out;
        }
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>();
            for (Object o : list) {
                out.add(maskValue(o));
            }
            return out;
        }
        return value;
    }

    private boolean isSensitive(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase());
    }
}
