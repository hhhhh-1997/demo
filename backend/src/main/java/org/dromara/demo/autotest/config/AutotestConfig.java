package org.dromara.demo.autotest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自动化测试执行引擎配置：串行线程池 + 关闭非 2xx 抛错的 RestClient。
 *
 * @author demo
 * @since 2026-08-15
 */
@Configuration
public class AutotestConfig {

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor autotestExecutor() {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "autotest-executor");
            t.setDaemon(true);
            return t;
        };
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100), factory);
    }

    @Bean
    public RestClient autotestRestClient() {
        return RestClient.builder()
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    // 关闭非 2xx 抛错：保留原始响应（含 4xx/5xx 的 body）供断言
                })
                .build();
    }
}
