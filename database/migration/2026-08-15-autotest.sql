-- =====================================================================
-- 接口链路自动化测试平台 — 增量迁移脚本
-- 日期: 2026-08-15
-- 说明: 对「已初始化」的本地/生产 MySQL(default_db) 增量应用本模块所需表结构与菜单权限。
--       前置: 库已执行过 database/schema.sql + database/data.sql 基线（或已含既有系统表）。
--       幂等: DDL 用 IF NOT EXISTS 可重复执行；下方 INSERT 为一次性种子，重复执行会因主键冲突报错。
-- =====================================================================

-- 1. 自动化测试-场景
CREATE TABLE IF NOT EXISTS at_scenario (
  id BIGINT AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  base_url VARCHAR(255),
  variables JSON,
  create_by BIGINT,
  create_time DATETIME,
  update_by BIGINT,
  update_time DATETIME,
  CONSTRAINT pk_at_scenario PRIMARY KEY (id)
);

-- 2. 自动化测试-步骤
CREATE TABLE IF NOT EXISTS at_step (
  id BIGINT AUTO_INCREMENT,
  scenario_id BIGINT NOT NULL,
  step_order INT NOT NULL,
  name VARCHAR(100),
  params JSON NOT NULL,
  create_time DATETIME,
  update_time DATETIME,
  CONSTRAINT pk_at_step PRIMARY KEY (id),
  KEY idx_at_step_scenario (scenario_id)
);

-- 3. 自动化测试-执行记录
CREATE TABLE IF NOT EXISTS at_run (
  id BIGINT AUTO_INCREMENT,
  scenario_id BIGINT,
  status TINYINT,
  fail_step_id BIGINT,
  error_msg VARCHAR(1000),
  start_time DATETIME,
  end_time DATETIME,
  trigger_by BIGINT,
  create_time DATETIME,
  CONSTRAINT pk_at_run PRIMARY KEY (id),
  KEY idx_at_run_scenario (scenario_id)
);

-- 4. 自动化测试-步骤结果
CREATE TABLE IF NOT EXISTS at_step_result (
  id BIGINT AUTO_INCREMENT,
  run_id BIGINT NOT NULL,
  step_id BIGINT,
  step_order INT,
  name VARCHAR(100),
  status TINYINT,
  request_snapshot JSON,
  response_snapshot JSON,
  assert_detail TEXT,
  error_msg VARCHAR(1000),
  start_time DATETIME,
  end_time DATETIME,
  CONSTRAINT pk_at_step_result PRIMARY KEY (id),
  KEY idx_at_step_result_run (run_id)
);

-- 5. 菜单（目录）+ 按钮（权限串）
INSERT INTO sys_menu (id, menu_name, parent_id, path, component, perms, menu_type, sort) VALUES
 (9,'自动化测试',0,'/autotest/scenario','autotest/scenario/index','','C',6);

INSERT INTO sys_menu (id, menu_name, parent_id, perms, menu_type, sort) VALUES
 (901,'场景新增',9,'autotest:scenario:add','F',1),
 (902,'场景编辑',9,'autotest:scenario:edit','F',2),
 (903,'场景删除',9,'autotest:scenario:delete','F',3),
 (904,'场景运行',9,'autotest:scenario:run','F',4),
 (905,'场景查看',9,'autotest:scenario:list','F',5);

-- 6. 授权给系统管理员（admin, role_id=1）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
 (1,9),(1,901),(1,902),(1,903),(1,904),(1,905);
