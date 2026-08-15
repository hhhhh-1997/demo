package org.dromara.demo.autotest.engine;

import org.dromara.demo.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {{变量}} 占位符替换。
 *
 * @author demo
 * @since 2026-08-15
 */
@Component
public class PlaceholderResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}\\}");

    public String resolve(String template, Map<String, Object> context) {
        if (template == null) {
            return null;
        }
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String name = m.group(1);
            if (!context.containsKey(name)) {
                throw new BusinessException("变量未定义: " + name);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(context.get(name))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public Map<String, String> resolveMap(Map<String, String> source, Map<String, Object> context) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : source.entrySet()) {
            result.put(e.getKey(), resolve(e.getValue(), context));
        }
        return result;
    }
}
