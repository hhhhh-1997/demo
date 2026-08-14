-- H2 种子（最小集：管理员登录所需的最小数据）
-- 部门
INSERT INTO sys_dept (id, dept_name, parent_id, sort) VALUES
 (1, '省公司', 0, 1);

-- 角色
INSERT INTO sys_role (id, role_name, role_key, sort) VALUES
 (1, '系统管理员', 'admin', 1);

-- 管理员用户（password 为 BCrypt("123456")，见 Task 4 生成后回填）
INSERT INTO sys_user (id, username, password, nickname, dept_id, create_by) VALUES
 (1, 'admin', '$2a$10$4JXwnETgUq5HQ5hCtAQ/HuEasFzSbw2Ekj6K1pO1gG.lwqZ/YfFcq', '系统管理员', 1, 1);

-- 用户-角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
