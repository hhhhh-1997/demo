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

-- 菜单（目录 + 菜单 + 按钮）
INSERT INTO sys_menu (id, menu_name, parent_id, path, component, perms, menu_type, sort) VALUES
 (1,'储备项目维护',0,'/project','project/index','','C',1),
 (2,'储备项目论证',0,'/review','review/index','','C',2),
 (3,'储备项目审核',0,'/audit','audit/index','','C',3),
 (4,'统一储备库',0,'/reserve','reserve/index','','C',4),
 (5,'系统管理',0,'/system','','','M',5),
 (6,'用户管理',5,'user','system/user/index','system:user','C',1),
 (7,'角色管理',5,'role','system/role/index','system:role','C',2),
 (8,'菜单管理',5,'menu','system/menu/index','system:menu','C',3),
 (9,'自动化测试',0,'/autotest/scenario','autotest/scenario/index','','C',6);
-- 按钮（F）
INSERT INTO sys_menu (id, menu_name, parent_id, perms, menu_type, sort) VALUES
 (101,'项目新增',1,'project:add','F',1),(102,'项目编辑',1,'project:edit','F',2),
 (103,'项目删除',1,'project:delete','F',3),(104,'项目提报',1,'project:submit','F',4),
 (105,'项目导出',1,'project:export','F',5),
 (201,'论证执行',2,'review:execute','F',1),(202,'论证查看',2,'review:view','F',2),
 (301,'审核执行',3,'audit:execute','F',1),(302,'批量审核',3,'audit:batch','F',2),
 (303,'审核查看',3,'audit:view','F',3),
 (401,'项目下达',4,'reserve:issue','F',1),(402,'储备库查看',4,'reserve:view','F',2),
 (403,'储备库导出',4,'reserve:export','F',3),
 (901,'场景新增',9,'autotest:scenario:add','F',1),
 (902,'场景编辑',9,'autotest:scenario:edit','F',2),
 (903,'场景删除',9,'autotest:scenario:delete','F',3),
 (904,'场景运行',9,'autotest:scenario:run','F',4),
 (905,'场景查看',9,'autotest:scenario:list','F',5);

-- 角色-菜单：admin 拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
