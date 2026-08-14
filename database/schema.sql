-- ============================================================================
-- 绩效月报查询模块 · 数据库初始化脚本（DDL）
-- 目标库   : default_db（见 docker-compose.yml / backend application.yml）
-- 数据来源 : data/人员月度绩效_2026-05至07.xlsx，全量 642 行
--            （2026-05: 216 ｜ 2026-06: 213 ｜ 2026-07: 213）
--
-- 重要口径（详见 docs/需求文档.md §7）：
--   1. 分值项 NULL = 「无该项工作」，数值 0 = 「有该项工作但得 0 分」，
--      二者语义相反；表结构必须保留 NULL 与 0 的区分，任何环节不得将 0 归并为 NULL。
--   2. pre_sales_support / test_quality 为「等级项」，存等级码 1~5
--      （1 超出预期 / 2 完全达标 / 3 需要提升 / 4 未达预期 / 5 无该项工作），
--      数值大小与优劣方向相反，不得作为分数求和/平均/按数值排序，映射在展示层完成。
--   3. month 为定宽字符串 YYYY-MM，直接字符串比较，不转日期类型。
-- ============================================================================

USE `default_db`;

DROP TABLE IF EXISTS `monthly_performance`;

CREATE TABLE `monthly_performance` (
    `id`                   INT          NOT NULL AUTO_INCREMENT COMMENT '记录 ID（主键）',
    `user_id`              INT          NOT NULL                COMMENT '用户 ID（与 month 组合唯一）',
    `realname`             VARCHAR(64)  NOT NULL                COMMENT '姓名（数据集中无工号字段）',
    `month`                VARCHAR(7)   NOT NULL                COMMENT '月份，定宽字符串 YYYY-MM，可直接字符串比较',

    `task_finish_rate`     DECIMAL(4,1) NOT NULL                COMMENT '任务完成率（分值项，全表无空值）',
    `work_effect_rate`     DECIMAL(4,1) NOT NULL                COMMENT '工作有效率（分值项，允许超 100、上限 120，全表无空值）',
    `work_normativity`     INT          NOT NULL                COMMENT '工作规范性（分值项，整型存储 0/10/…/100，展示保留一位小数）',
    `learning_improvement` DECIMAL(4,1) NULL                    COMMENT '学习及能力提升（分值项，仅 21 行有值且全为 100）',
    `software_design`      DECIMAL(4,1) NULL                    COMMENT '软需设计（分值项）',
    `pre_sales_support`    TINYINT      NULL                    COMMENT '售前支撑（等级项，等级码 1~5，映射在展示层）',
    `bug_condition`        DECIMAL(4,1) NULL                    COMMENT '缺陷情况（分值项）',
    `system_design`        DECIMAL(4,1) NULL                    COMMENT '概要设计（分值项）',
    `code_review`          DECIMAL(4,1) NULL                    COMMENT '代码评审（分值项，量纲 0~100，全表仅 3 行有值）',
    `test_quality`         TINYINT      NULL                    COMMENT '测试产出质量（等级项，等级码 1~5，映射在展示层）',

    `dept`                 INT          NOT NULL                COMMENT '二级部门 ID（与一级部门严格一对一）',
    `role`                 VARCHAR(32)  NOT NULL                COMMENT '岗位 key（所有岗位判断按 key 进行）',
    `role_name`            VARCHAR(64)  NOT NULL                COMMENT '岗位名称（由 key 映射生成，冗余存储自源导出）',
    `dept_name`            VARCHAR(64)  NOT NULL                COMMENT '二级部门名称',
    `top_dept_id`          INT          NOT NULL                COMMENT '一级部门 ID（筛选按此字段）',
    `top_dept_name`        VARCHAR(64)  NOT NULL                COMMENT '一级部门名称（仅用于展示）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_month` (`user_id`, `month`),
    KEY `idx_top_dept_id` (`top_dept_id`),
    KEY `idx_role` (`role`),
    KEY `idx_month` (`month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  COMMENT = '人员月度绩效表（绩效月报查询模块）';
