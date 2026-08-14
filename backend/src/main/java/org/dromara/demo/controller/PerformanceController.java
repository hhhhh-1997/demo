package org.dromara.demo.controller;

import org.dromara.demo.common.Result;
import org.dromara.demo.domain.Performance;
import org.dromara.demo.dto.PerformanceQuery;
import org.dromara.demo.service.PerformanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService service;

    public PerformanceController(PerformanceService service) {
        this.service = service;
    }

    @GetMapping("/monthly")
    public Result<List<Performance>> monthly(PerformanceQuery query) {
        return Result.ok(service.list(query));
    }
}
