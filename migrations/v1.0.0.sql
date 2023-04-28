/* @Deprecated 20160808 */
-- user
DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  username        VARCHAR(20) NOT NULL COMMENT '花名拼音',
  nickname        VARCHAR(20) NOT NULL COMMENT '花名',
  email           VARCHAR(50) NOT NULL COMMENT '邮箱',
  mobile          VARCHAR(15),
  gmt_create      DATETIME NOT NULL,
  gmt_modified    DATETIME,
  CONSTRAINT PK_USER PRIMARY KEY (id),
  CONSTRAINT UK_USER_USERNAME UNIQUE (username),
  CONSTRAINT UK_USER_EMAIL UNIQUE (email)
) COMMENT '用户表';

DROP TABLE IF EXISTS role;
CREATE TABLE role (
  id        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`    VARCHAR(255) NOT NULL COMMENT '角色编码',
  `name`    VARCHAR(30) NOT NULL COMMENT '角色名称',
  CONSTRAINT UK_ROLE_CODE UNIQUE (`code`),
  CONSTRAINT PK_ROLE PRIMARY KEY (id)
) COMMENT '角色表';

DROP TABLE IF EXISTS privilege;
CREATE TABLE privilege (
  id        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  code      VARCHAR(30) NOT NULL COMMENT '权限编码',
  name      VARCHAR(30) NOT NULL COMMENT '权限名称',
  `desc`    VARCHAR(255) NOT NULL COMMENT '描述',
  CONSTRAINT PK_PRIVILEGE PRIMARY KEY (id),
  CONSTRAINT UK_PRIVILEGE_CODE UNIQUE (code)
) COMMENT '权限表';

DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role (
  user_id    INT UNSIGNED,
  role_id    INT UNSIGNED,
  CONSTRAINT UK_USER_ROLE UNIQUE (user_id, role_id)
) COMMENT '用户角色关系表';

DROP TABLE IF EXISTS role_privilege;
CREATE TABLE role_privilege (
  role_id    INT UNSIGNED,
  priv_id    INT UNSIGNED,
  CONSTRAINT UK_ROLE_PRIVILEGE UNIQUE (role_id, priv_id)
) COMMENT '角色权限关系表';

-- dms
CREATE TABLE `datasource_proxy` (
  `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(30) NOT NULL COMMENT '数据源代理名称',
  `host`          VARCHAR(64) NOT NULL COMMENT '数据库主机域名，或者IP',
  `gmt_create`    DATETIME NOT NULL COMMENT '创建时间',
  `gmt_modified`  DATETIME DEFAULT NULL COMMENT '修改时间',
  CONSTRAINT PK_DATASOURCE_PROXY PRIMARY KEY (`id`),
  CONSTRAINT UK_DATASOURCE_PROXY_NAME UNIQUE (`name`),
  CONSTRAINT UK_DATASOURCE_PROXY_HOST UNIQUE (`host`)
) COMMENT '数据源代理表';

CREATE TABLE `datasource` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(30) DEFAULT NULL COMMENT '数据源名称，比如：贷后系统数据库',
  `type`            VARCHAR(10) DEFAULT NULL COMMENT '数据库类型',
  `env`             VARCHAR(10) DEFAULT NULL COMMENT '环境',

  `sid`             VARCHAR(30) DEFAULT NULL COMMENT '数据库SID',
  `host`            VARCHAR(30) DEFAULT NULL COMMENT '数据库主机域名，或者IP',
  `port`            INT DEFAULT NULL COMMENT '端口号',
  `username`        VARCHAR(30) DEFAULT NULL COMMENT '用户名',
  `password`        VARCHAR(256) DEFAULT NULL COMMENT '密码',

  `sid2`            VARCHAR(30) DEFAULT NULL COMMENT '数据库备机SID',
  `host2`           VARCHAR(30) DEFAULT NULL COMMENT '数据库备机主机域名，或者IP',
  `port2`           INT DEFAULT NULL COMMENT '备机端口号',
  `username2`       VARCHAR(30) DEFAULT NULL COMMENT '备机用户名',
  `password2`       VARCHAR(256) DEFAULT NULL COMMENT '备机密码',

  `proxy_id`        INT(10) UNSIGNED COMMENT '代理id',
  `proxy_port`      INT(11) COMMENT '代理端口号',
  `proxy_sid`       VARCHAR(30) COMMENT '代理数据库SID',
  `proxy_username`  VARCHAR(30) COMMENT '代理用户名',
  `proxy_password`  VARCHAR(256) COMMENT '代理密码',

  `gmt_create`    DATETIME NOT NULL COMMENT '创建时间',
  `gmt_modified`  DATETIME DEFAULT NULL COMMENT '修改时间',
  CONSTRAINT PK_DATASOURCE PRIMARY KEY (`id`),
  CONSTRAINT UK_DATASOURCE_NAME UNIQUE (`env`, `name`),
  CONSTRAINT UK_DATASOURCE_SID UNIQUE (`env`, `sid`)
) COMMENT '数据源配置表';

CREATE TABLE `datasource_role` (
  `id`      INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`    VARCHAR(30) NOT NULL,
  `name`    VARCHAR(30) NOT NULL,
  `order`   TINYINT NOT NULL,
  CONSTRAINT PK_DATASOURCE_ROLE PRIMARY KEY (id),
  CONSTRAINT UK_DATASOURCE_ROLE UNIQUE (`code`)
) COMMENT '数据源角色表';

CREATE TABLE `datasource_auth` (
  `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `datasource_id`  INT UNSIGNED NOT NULL COMMENT '数据源id',
  `user_id`        INT UNSIGNED NOT NULL COMMENT '用户id',
  `user_name`      VARCHAR(20) NOT NULL COMMENT '冗余花名拼音',
  `gmt_auth`       DATETIME NOT NULL COMMENT '授权时间',
  CONSTRAINT PK_DATASOURCE_AUTH PRIMARY KEY (`id`),
  CONSTRAINT UK_DATASOURCE_AUTH UNIQUE (`datasource_id`, `user_id`),
  CONSTRAINT FK_DATASOURCE_AUTH_DS FOREIGN KEY (`datasource_id`) REFERENCES `datasource` (`id`) ON DELETE CASCADE
) COMMENT '数据源授权表';

CREATE TABLE `datasource_auth_role` (
  auth_id          INT UNSIGNED NOT NULL,
  role             VARCHAR(30) NOT NULL,
  CONSTRAINT UK_DATASOURCE_AUTH_ROLE UNIQUE (auth_id, role),
  CONSTRAINT FK_DATASOURCE_AUTH_ROLE FOREIGN KEY (`auth_id`) REFERENCES `datasource_auth` (`id`) ON DELETE CASCADE,
  CONSTRAINT FK_DATASOURCE_AUTH_ROLE_1 FOREIGN KEY (`role`) REFERENCES `datasource_role` (`code`) ON DELETE CASCADE
) COMMENT '数据源角色关系表';


CREATE TABLE `sql_history` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `datasource_id`   INT UNSIGNED NOT NULL COMMENT '数据源id',
  `user_id`         INT UNSIGNED NOT NULL COMMENT '用户id',
  `type`            VARCHAR(20) NOT NULL COMMENT 'SQL类型，select/update/insert',
  `sql`             TEXT NOT NULL,
  `exec_sql`        TEXT NOT NULL COMMENT '执行的sql',
  `exec_hash`       VARCHAR(50) NOT NULL COMMENT '执行的sql hash',
  `exec_time`       INT NOT NULL COMMENT '耗时，单位为ms',
  `count`           INT NOT NULL COMMENT '影响行数',
  `gmt_create`      DATETIME NOT NULL COMMENT '创建时间',
  CONSTRAINT PK_SQL_HISTORY PRIMARY KEY (`id`),
  CONSTRAINT FK_SQL_HISTORY_DS FOREIGN KEY (`datasource_id`) REFERENCES `datasource` (`id`) ON DELETE CASCADE
) COMMENT 'SQL查询历史记录';

CREATE TABLE `task_biz` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `proc_inst_id`    VARCHAR(64) COMMENT 'process instance id',
  `type`            VARCHAR(20) NOT NULL COMMENT 'task类型，ds-apply/data-export/data-change',
  `start_user`      VARCHAR(30) DEFAULT NULL COMMENT '用户名',
  `status`          VARCHAR(10) NOT NULL COMMENT 'process/end',
  `start_time`      DATETIME NOT NULL,
  `end_time`        DATETIME,
  `info`            VARCHAR(255),
  `explain`         VARCHAR(1000),
  CONSTRAINT PK_TASK_BIZ PRIMARY KEY (`id`)
) COMMENT '任务业务表';

CREATE TABLE `task_ds_apply` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `task_id`         INT UNSIGNED NOT NULL,
  `key`             VARCHAR(30) NOT NULL,
  reason            VARCHAR(1000) NOT NULL,

  assessor          VARCHAR(20) COMMENT '审查人花名拼音',
  assess_time       DATETIME,
  assess_type       TINYINT,
  assess_remark     VARCHAR(255),
  CONSTRAINT PK_TASK_DS_APPLY PRIMARY KEY (`id`),
  CONSTRAINT UK_TASK_DS_APPLY UNIQUE (task_id, `key`)
) COMMENT '数据源申请业务表';

CREATE TABLE `task_data_export` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `task_id`         INT UNSIGNED NOT NULL,
  `key`             VARCHAR(30) NOT NULL,
  reason            VARCHAR(1000) NOT NULL,

  `sql`             TEXT NOT NULL,
  security          TINYINT NOT NULL COMMENT '是否脱敏',
  execute_success   TINYINT,
  affect_size       INT UNSIGNED,
  message           TEXT,
  data_file         VARCHAR(1000),

  assessor          VARCHAR(20) COMMENT '审查人花名拼音',
  assess_time       DATETIME,
  assess_type       TINYINT,
  assess_remark     VARCHAR(255),
  CONSTRAINT PK_TASK_DATA_EXPORT PRIMARY KEY (`id`),
  CONSTRAINT UK_TASK_DATA_EXPORT UNIQUE (task_id, `key`)
) COMMENT '数据导出业务表';

CREATE TABLE `task_data_change` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `task_id`         INT UNSIGNED NOT NULL,
  `key`             VARCHAR(30) NOT NULL,
  reason            VARCHAR(1000) NOT NULL,

  `sql`             TEXT NOT NULL,
  execute_success   TINYINT,
  affect_size       INT UNSIGNED,
  message           TEXT,
  rollback_sql_file VARCHAR(1000),
  backup_file       TEXT,

  assessor          VARCHAR(20) COMMENT '审查人花名拼音',
  assess_time       DATETIME,
  assess_type       TINYINT,
  assess_remark     VARCHAR(255),
  CONSTRAINT PK_TASK_DATA_CHANGE PRIMARY KEY (`id`),
  CONSTRAINT UK_TASK_DATA_CHANGE UNIQUE (task_id, `key`)
) COMMENT '数据变更业务表';

CREATE TABLE `task_struct_change` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `task_id`         INT UNSIGNED NOT NULL COMMENT '任务id',
  `key`             VARCHAR(30) NOT NULL COMMENT '任务key,通常是子任务数据源',
  reason            VARCHAR(1000) NOT NULL COMMENT '任务申请原因',

  change_type       TINYINT  NOT NULL DEFAULT 1 COMMENT '数据变更类型',
  `sql`             TEXT NOT NULL COMMENT 'ddl sql',

  executor          VARCHAR(20) COMMENT '执行人花名拼音',
  execute_time      DATETIME COMMENT '执行时间',
  execute_type      TINYINT COMMENT '执行结果',
  execute_remark    VARCHAR(255) COMMENT '执行备注',

  assessor          VARCHAR(20) COMMENT '审查人花名拼音',
  assess_time       DATETIME COMMENT '审核时间',
  assess_type       TINYINT COMMENT '审核类型',
  assess_remark     VARCHAR(255) COMMENT '审核备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_task_key(task_id, `key`)
) COMMENT '结构变更业务表';


CREATE TABLE `security_data` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `datasource_id`   INT UNSIGNED NOT NULL,
  `table`           VARCHAR(64) NOT NULL,
  `column`          VARCHAR(64) NOT NULL,
  `gmt_create`      DATETIME NOT NULL COMMENT '创建时间',
  CONSTRAINT PK_SECURITY_DATA PRIMARY KEY (`id`),
  CONSTRAINT FK_SECURITY_DATA_DS FOREIGN KEY (`datasource_id`) REFERENCES `datasource` (`id`) ON DELETE CASCADE
) COMMENT '敏感数据配置表';

CREATE TABLE `security_data_auth` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `security_id`     INT UNSIGNED NOT NULL,
  `user_name`       VARCHAR(20) NOT NULL COMMENT '花名拼音',
  `gmt_create`      DATETIME NOT NULL COMMENT '创建时间',
  CONSTRAINT PK_SECURITY_DATA_AUTH PRIMARY KEY (`id`),
  CONSTRAINT FK_SECURITY_DATA_AUTH_SECURITY FOREIGN KEY (`security_id`) REFERENCES `security_data` (`id`) ON DELETE CASCADE
) COMMENT '敏感数据授权表';

CREATE TABLE `operate_log` (
  `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `operator`       VARCHAR(50) NOT NULL COMMENT '操作人',
  `type`           TINYINT NOT NULL COMMENT '操作日志类型',
  `type_string`    VARCHAR(30) NOT NULL,
  `ip`             VARCHAR(30) NOT NULL COMMENT '操作IP',
  `content`        MEDIUMTEXT  NOT NULL COMMENT '操作内容',
  `time`           DATETIME NOT NULL COMMENT '创建时间',
  CONSTRAINT PK_OPERATE_LOG PRIMARY KEY (`id`),
  KEY `IDX_LOG_TIME_OPERATOR` (`time`, `operator`)
) COMMENT '用户操作日志表';

-- sys
CREATE TABLE sys_config (
  `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `key`           VARCHAR(30) NOT NULL COMMENT 'config key',
  `value`         VARCHAR(2000) NOT NULL COMMENT 'config value',
  CONSTRAINT PK_SYS_CONFIG PRIMARY KEY (id),
  CONSTRAINT UK_SYS_CONFIG_KEY UNIQUE (`key`)
) COMMENT '系统配置表';
