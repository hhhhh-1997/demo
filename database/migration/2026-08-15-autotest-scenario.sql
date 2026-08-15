-- =====================================================================
-- 接口链路自动化测试 — 完整业务链路种子
-- 日期: 2026-08-15
-- 说明: 初始化一条「登录 → 新建项目 → 提报 → 论证 → 审核 → 下达 → 登出」的测试场景。
--       前置: 已执行 database/migration/2026-08-15-autotest.sql（at_ 表与菜单已建）。
--       端口: 后端 server.port = 8081，故 base_url 用 http://localhost:8081。
--       说明: 提报必须作用于已有项目，故链路在登录后新增一步「新建项目」并提取项目 id。
--       注意: 本脚本为一次性种子；每次执行会新建一条场景，运行该场景每次会新建一个项目。
-- =====================================================================

INSERT INTO at_scenario (name, description, base_url, variables, create_by, create_time, update_by, update_time)
VALUES (
  '完整业务链路：登录→新建→提报→论证→审核→下达→登出',
  '端到端验证储备项目全生命周期',
  'http://localhost:8081',
  '{"username":"admin","password":"123456"}',
  1, NOW(), 1, NOW()
);

SET @sid = LAST_INSERT_ID();

INSERT INTO at_step (scenario_id, step_order, name, params, create_time, update_time) VALUES
(@sid, 1, '登录',
 '{"method":"POST","path":"/api/auth/login","headers":{"Content-Type":"application/json"},"query":{},"body":{"username":"{{username}}","password":"{{password}}"},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[{"name":"token","jsonPath":"$.data.token"}]}',
 NOW(), NOW()),

(@sid, 2, '新建项目',
 '{"method":"POST","path":"/api/project","headers":{"Content-Type":"application/json","Authorization":"{{token}}"},"query":{},"body":{"projectName":"自动化测试链路项目","projectType":"电网基建","investmentAmount":1000000,"deptId":1,"description":"自动化测试完整链路创建"},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[{"name":"projectId","jsonPath":"$.data"}]}',
 NOW(), NOW()),

(@sid, 3, '提报',
 '{"method":"POST","path":"/api/project/submit/{{projectId}}","headers":{"Authorization":"{{token}}"},"query":{},"body":{},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[]}',
 NOW(), NOW()),

(@sid, 4, '论证',
 '{"method":"POST","path":"/api/review/{{projectId}}/execute","headers":{"Content-Type":"application/json","Authorization":"{{token}}"},"query":{},"body":{"infoComplete":"完整","threeImportant":"符合","splitProject":"无","interfaceConfusion":"无","opinion":"同意","pass":true},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[]}',
 NOW(), NOW()),

(@sid, 5, '审核',
 '{"method":"POST","path":"/api/audit/{{projectId}}/execute","headers":{"Content-Type":"application/json","Authorization":"{{token}}"},"query":{},"body":{"opinion":"同意","pass":true},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[]}',
 NOW(), NOW()),

(@sid, 6, '下达',
 '{"method":"POST","path":"/api/reserve/{{projectId}}/issue","headers":{"Authorization":"{{token}}"},"query":{},"body":{},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[]}',
 NOW(), NOW()),

(@sid, 7, '登出',
 '{"method":"POST","path":"/api/auth/logout","headers":{"Authorization":"{{token}}"},"query":{},"body":{},"asserts":[{"type":"STATUS","expected":"200"},{"type":"JSON","jsonPath":"$.code","op":"EQUALS","expected":"200"}],"extracts":[]}',
 NOW(), NOW());
