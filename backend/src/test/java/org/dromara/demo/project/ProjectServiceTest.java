package org.dromara.demo.project;

import org.dromara.demo.common.BusinessException;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.domain.ProjectStatus;
import org.dromara.demo.project.mapper.ProjectMapper;
import org.dromara.demo.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 项目状态机与编号生成单元测试。
 *
 * @author demo
 * @since 2026-08-14
 */
class ProjectServiceTest {

    private ProjectMapper mapper;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(ProjectMapper.class);
        service = new ProjectService(mapper);
    }

    private Project p(String status) {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(status);
        return project;
    }

    @Test
    void submit_shouldMoveDraftToPendingReview() {
        Project project = p("草稿");
        Mockito.when(mapper.selectById(1L)).thenReturn(project);
        service.submit(1L);
        assertEquals(ProjectStatus.PENDING_REVIEW.getCode(), project.getStatus());
    }

    @Test
    void submit_shouldAllowReviewRejected() {
        Project project = p("论证退回");
        Mockito.when(mapper.selectById(1L)).thenReturn(project);
        service.submit(1L);
        assertEquals(ProjectStatus.PENDING_REVIEW.getCode(), project.getStatus());
    }

    @Test
    void submit_shouldRejectAlreadyIssued() {
        Project project = p("已下达");
        Mockito.when(mapper.selectById(1L)).thenReturn(project);
        assertThrows(BusinessException.class, () -> service.submit(1L));
    }

    @Test
    void issue_shouldMovePendingIssueToIssued() {
        Project project = p("待下达");
        Mockito.when(mapper.selectById(1L)).thenReturn(project);
        service.issue(1L);
        assertEquals(ProjectStatus.ISSUED.getCode(), project.getStatus());
        assertNotNull(project.getIssueTime());
    }

    @Test
    void issue_shouldRejectDraft() {
        Project project = p("草稿");
        Mockito.when(mapper.selectById(1L)).thenReturn(project);
        assertThrows(BusinessException.class, () -> service.issue(1L));
    }

    @Test
    void generateCode_shouldProduceXmPrefix() {
        Mockito.when(mapper.selectMaxCodeByMonth(Mockito.anyString())).thenReturn(null);
        String code = service.generateCode();
        assertTrue(code.startsWith("XM"));
        assertTrue(code.endsWith("001"));
    }

    @Test
    void generateCode_shouldIncrementExistingMax() {
        String prefix = "XM" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Mockito.when(mapper.selectMaxCodeByMonth(Mockito.anyString())).thenReturn(prefix + "005");
        String code = service.generateCode();
        assertEquals(prefix + "006", code);
    }
}
