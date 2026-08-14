package org.dromara.demo.project;

import org.dromara.demo.common.BusinessException;
import org.dromara.demo.project.domain.ProjectStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 状态枚举测试。
 *
 * @author demo
 * @since 2026-08-14
 */
class ProjectStatusTest {

    @Test
    void fromCode_shouldResolveAllSevenStates() {
        assertEquals(ProjectStatus.DRAFT, ProjectStatus.fromCode("草稿"));
        assertEquals(ProjectStatus.PENDING_REVIEW, ProjectStatus.fromCode("待论证"));
        assertEquals(ProjectStatus.REVIEW_REJECTED, ProjectStatus.fromCode("论证退回"));
        assertEquals(ProjectStatus.PENDING_AUDIT, ProjectStatus.fromCode("待审核"));
        assertEquals(ProjectStatus.AUDIT_REJECTED, ProjectStatus.fromCode("审核退回"));
        assertEquals(ProjectStatus.PENDING_ISSUE, ProjectStatus.fromCode("待下达"));
        assertEquals(ProjectStatus.ISSUED, ProjectStatus.fromCode("已下达"));
    }

    @Test
    void fromCode_shouldThrowOnUnknown() {
        assertThrows(BusinessException.class, () -> ProjectStatus.fromCode("不存在"));
    }
}
