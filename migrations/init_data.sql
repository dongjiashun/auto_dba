# privilege
INSERT INTO `privilege` (`id`, `code`, `name`, `desc`)
VALUES
  (1, 'SECURITY_MANAGE', '敏感数据管理', '敏感数据管理'),
  (2, 'DATASOURCE_MANAGE', '数据源管理', '数据源管理'),
  (3, 'TASK_MANAGE', '任务管理', '任务管理'),
  (4, 'USER_MANAGE', '用户管理', '用户管理'),
  (5, 'SYS_MANAGE', '系统管理', '系统管理');

# role
INSERT INTO `role` (`id`, `code`, `name`)
VALUES
  (1, 'admin', '管理员'),
  (2, 'dba', 'DBA');

# role_privilege
INSERT INTO `role_privilege` (`role_id`, `priv_id`)
VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (1, 4),
  (1, 5),
  (2, 1),
  (2, 2),
  (2, 3);

# datasource_role
INSERT INTO `datasource_role` (`id`, `code`, `name`, `order`)
VALUES
  (1,'dev','开发',10),
  (2,'exporter','导出审核',20),
  (3,'reviewer','开发审核',30),
  (4,'owner','拥有者',40);
