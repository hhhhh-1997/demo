package org.dromara.demo.controller;

import org.dromara.demo.common.GlobalExceptionHandler;
import org.dromara.demo.domain.Performance;
import org.dromara.demo.service.PerformanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PerformanceControllerTest {

    private PerformanceService service;

    @BeforeEach
    void setUp() {
        service = mock(PerformanceService.class);
    }

    @Test
    void monthly_returnsUnifiedResult() throws Exception {
        when(service.list(any())).thenReturn(List.of(new Performance()));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PerformanceController(service)).build();
        mvc.perform(get("/api/performance/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void monthly_bindsQueryParams() throws Exception {
        when(service.list(any())).thenReturn(List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PerformanceController(service)).build();
        mvc.perform(get("/api/performance/monthly")
                        .param("topDeptId", "10")
                        .param("role", "dev")
                        .param("month", "2026-05"))
                .andExpect(status().isOk());
        verify(service).list(argThat(q ->
                q.getTopDeptId() == 10
                        && "dev".equals(q.getRole())
                        && "2026-05".equals(q.getMonth())));
    }

    @Test
    void monthly_error_mapsTo500WithNullData() throws Exception {
        when(service.list(any())).thenThrow(new RuntimeException("boom"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PerformanceController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mvc.perform(get("/api/performance/monthly"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
