-- 部门（沿用 sys_department.csv）
INSERT INTO sys_dept (id, dept_name, parent_id, sort) VALUES
 (1,'省公司',0,1),(2,'成都供电公司',1,2),(3,'绵阳供电公司',1,3),
 (4,'宜宾供电公司',1,4),(5,'德阳供电公司',1,5),(6,'乐山供电公司',1,6),
 (7,'自贡供电公司',1,7),(8,'省公司运检部',1,8),(9,'省公司安监部',1,9),
 (10,'省公司信息中心',1,10),(11,'省公司科技部',1,11);

-- 字典类型
INSERT INTO sys_dict_type (id, dict_name, dict_type) VALUES
 (1,'项目分类','project_type'),(2,'项目状态','project_status'),
 (3,'项目来源','project_source'),(4,'论证结论','review_result'),(5,'审核结论','audit_result');

-- 字典数据：project_type 统一 8 类
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
 ('project_type','电网基建','电网基建',1),('project_type','生产设备技改','生产设备技改',2),
 ('project_type','科技创新','科技创新',3),('project_type','营销投入','营销投入',4),
 ('project_type','信息化建设','信息化建设',5),('project_type','固定资产零购','固定资产零购',6),
 ('project_type','战新产业','战新产业',7),('project_type','生产辅助','生产辅助',8);

-- project_status 7 态
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
 ('project_status','草稿','草稿',1),('project_status','待论证','待论证',2),
 ('project_status','论证退回','论证退回',3),('project_status','待审核','待审核',4),
 ('project_status','审核退回','审核退回',5),('project_status','待下达','待下达',6),
 ('project_status','已下达','已下达',7);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
 ('project_source','手动录入','MANUAL',1),('project_source','外部系统拉取','SYNC',2),('project_source','Excel导入','IMPORT',3),
 ('review_result','通过','通过',1),('review_result','不通过','不通过',2),
 ('audit_result','通过','通过',1),('audit_result','退回','退回',2);

-- 角色
INSERT INTO sys_role (id, role_name, role_key, sort) VALUES
 (1,'系统管理员','admin',1),(2,'维护员','maintainer',2),(3,'论证员','reviewer',3),
 (4,'审核员','auditor',4),(5,'储备库管理员','reserve',5);

-- 菜单（目录 + 菜单 + 按钮）
INSERT INTO sys_menu (id, menu_name, parent_id, path, component, perms, menu_type, sort) VALUES
 (1,'储备项目维护',0,'/project','project/index','','C',1),
 (2,'储备项目论证',0,'/review','review/index','','C',2),
 (3,'储备项目审核',0,'/audit','audit/index','','C',3),
 (4,'统一储备库',0,'/reserve','reserve/index','','C',4),
 (5,'系统管理',0,'/system','','','M',5),
 (6,'用户管理',5,'user','system/user/index','system:user','C',1),
 (7,'角色管理',5,'role','system/role/index','system:role','C',2),
 (8,'菜单管理',5,'menu','system/menu/index','system:menu','C',3);
-- 按钮（F）
INSERT INTO sys_menu (id, menu_name, parent_id, perms, menu_type, sort) VALUES
 (101,'项目新增',1,'project:add','F',1),(102,'项目编辑',1,'project:edit','F',2),
 (103,'项目删除',1,'project:delete','F',3),(104,'项目提报',1,'project:submit','F',4),
 (105,'项目导出',1,'project:export','F',5),
 (201,'论证执行',2,'review:execute','F',1),(202,'论证查看',2,'review:view','F',2),
 (301,'审核执行',3,'audit:execute','F',1),(302,'批量审核',3,'audit:batch','F',2),
 (303,'审核查看',3,'audit:view','F',3),
 (401,'项目下达',4,'reserve:issue','F',1),(402,'储备库查看',4,'reserve:view','F',2),
 (403,'储备库导出',4,'reserve:export','F',3);

-- 角色-菜单：admin 全部；其余按模块
-- admin 拥有所有菜单（1-8 + 101-403），此处用子查询简洁表达
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
 (2,1),(2,101),(2,102),(2,103),(2,104),(2,105),
 (3,2),(3,201),(3,202),
 (4,3),(4,301),(4,302),(4,303),
 (5,4),(5,401),(5,402),(5,403);

-- 管理员用户（password 为 BCrypt("123456")，见 Task 4 生成后回填）
INSERT INTO sys_user (id, username, password, nickname, dept_id, create_by) VALUES
 (1,'admin','<BCRYPT_123456>','系统管理员',1,1);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1,1);
