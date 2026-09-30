-- ============================================================
-- XuYa Plus v1.1.0 升级脚本：文件管理 / 定时任务 / 监控 / 代码生成
-- 在 sql/xuya_plus.sql 全量初始化之后执行
-- ============================================================
USE xuya_plus;

-- ----------------------------
-- OSS 对象存储配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_oss_config;
CREATE TABLE sys_oss_config (
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_key  varchar(20)  NOT NULL                COMMENT '配置标识（minio/local）',
    config_name varchar(50)  DEFAULT ''              COMMENT '配置名称',
    access_key  varchar(255) DEFAULT ''              COMMENT 'accessKey',
    secret_key  varchar(255) DEFAULT ''              COMMENT 'secretKey',
    bucket_name varchar(255) DEFAULT ''              COMMENT '桶名称',
    endpoint    varchar(255) DEFAULT ''              COMMENT '端点（含协议，如 http://localhost:9000）',
    domain      varchar(255) DEFAULT ''              COMMENT '自定义访问域名（留空用端点拼装）',
    region      varchar(50)  DEFAULT ''              COMMENT '区域（MinIO 可留空）',
    storage_path varchar(255) DEFAULT ''             COMMENT '本地存储根路径（config_key=local 时生效）',
    status      tinyint      DEFAULT 1               COMMENT '是否当前使用（0使用中 1备用）',
    remark      varchar(255) DEFAULT NULL            COMMENT '备注',
    create_by   varchar(64)  DEFAULT ''              COMMENT '创建者',
    create_time datetime     DEFAULT NULL            COMMENT '创建时间',
    update_by   varchar(64)  DEFAULT ''              COMMENT '更新者',
    update_time datetime     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_oss_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OSS对象存储配置表';

-- ----------------------------
-- OSS 文件表
-- ----------------------------
DROP TABLE IF EXISTS sys_oss;
CREATE TABLE sys_oss (
    id           bigint       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    file_name    varchar(255) DEFAULT ''              COMMENT '对象名（含日期路径）',
    original_name varchar(255) DEFAULT ''             COMMENT '原始文件名',
    file_suffix  varchar(20)  DEFAULT ''              COMMENT '文件后缀',
    url          varchar(500) DEFAULT ''              COMMENT '访问地址',
    service      varchar(20)  DEFAULT ''              COMMENT '存储标识（sys_oss_config.config_key）',
    file_size    bigint       DEFAULT 0               COMMENT '文件大小（字节）',
    create_by    varchar(64)  DEFAULT ''              COMMENT '上传人',
    create_time  datetime     DEFAULT NULL            COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_oss_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- ----------------------------
-- 定时任务调度表
-- ----------------------------
DROP TABLE IF EXISTS sys_job;
CREATE TABLE sys_job (
    id              bigint       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    job_name        varchar(64)  DEFAULT ''              COMMENT '任务名称',
    job_group       varchar(64)  DEFAULT 'DEFAULT'       COMMENT '任务组名',
    invoke_target   varchar(500) NOT NULL                COMMENT '调用目标（beanName.method(参数)）',
    cron_expression varchar(255) DEFAULT ''              COMMENT 'cron执行表达式',
    concurrent      tinyint      DEFAULT 1               COMMENT '是否并发执行（0允许 1禁止）',
    status          tinyint      DEFAULT 0               COMMENT '状态（0正常 1暂停）',
    remark          varchar(255) DEFAULT ''              COMMENT '备注',
    create_by       varchar(64)  DEFAULT ''              COMMENT '创建者',
    create_time     datetime     DEFAULT NULL            COMMENT '创建时间',
    update_by       varchar(64)  DEFAULT ''              COMMENT '更新者',
    update_time     datetime     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

-- ----------------------------
-- 定时任务调度日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_job_log;
CREATE TABLE sys_job_log (
    id            bigint        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    job_id        bigint        DEFAULT NULL           COMMENT '任务ID',
    job_name      varchar(64)   DEFAULT ''             COMMENT '任务名称',
    job_group     varchar(64)   DEFAULT ''             COMMENT '任务组名',
    invoke_target varchar(500)  DEFAULT ''             COMMENT '调用目标',
    job_message   varchar(500)  DEFAULT ''             COMMENT '日志信息',
    status        tinyint       DEFAULT 0              COMMENT '执行状态（0成功 1失败）',
    exception_info varchar(2000) DEFAULT ''            COMMENT '异常信息',
    create_time   datetime      DEFAULT NULL           COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_job_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度日志表';

-- OSS 配置种子：本地存储（使用中）+ MinIO（真实联调环境，随用随切）
INSERT INTO sys_oss_config (id, config_key, config_name, access_key, secret_key, bucket_name, endpoint, domain, region, storage_path, status, remark, create_time) VALUES
(1, 'local', '本地存储', '', '', '', '', '', '', './uploads', 1, '本地磁盘存储（开发/单机）', NOW()),
(2, 'minio', 'MinIO',   'vaulthive', 'vaulthive123', 'xuya-plus', 'http://localhost:9000', '', '', '', 0, 'WSL MinIO 对象存储', NOW());

-- 定时任务示例（演示 JobInvokeUtil 反射调用，任务组 DEFAULT）
INSERT INTO sys_job (id, job_name, job_group, invoke_target, cron_expression, concurrent, status, remark, create_time) VALUES
(1, '示例任务：系统信息采样', 'DEFAULT', 'jobDemoTask.sample()', '0 0/30 * * * ?', 1, 1, '每30分钟执行一次（默认暂停，可开启）', NOW());

-- ----------------------------
-- 菜单：文件管理 / OSS配置 / 定时任务 / 服务监控 / 代码生成
-- ----------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, visible, status, create_time) VALUES
(3,   '系统工具', 0, 3, 'tool',      NULL,                     'M', NULL,                 'tool',      '0', 0, NOW()),
(112, '任务调度', 2, 5, 'job',       'monitor/job/index',      'C', 'sys:job:list',       'job',       '0', 0, NOW()),
(113, '服务监控', 2, 6, 'server',    'monitor/server/index',   'C', 'sys:server:list',    'server',    '0', 0, NOW()),
(120, '文件管理', 1, 9, 'oss',       'system/oss/index',       'C', 'sys:oss:list',       'upload',    '0', 0, NOW()),
(124, 'OSS配置', 1, 10, 'oss-config','system/oss-config/index','C', 'sys:oss-config:list','edit',      '0', 0, NOW()),
(131, '代码生成', 3, 1, 'gen',       'tool/gen/index',         'C', 'sys:gen:list',       'code',      '0', 0, NOW());

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, visible, status, create_time) VALUES
-- 任务调度按钮
(1201, '任务查询', 112, 1, '', NULL, 'F', 'sys:job:detail',  '#', '0', 0, NOW()),
(1202, '任务新增', 112, 2, '', NULL, 'F', 'sys:job:add',     '#', '0', 0, NOW()),
(1203, '任务修改', 112, 3, '', NULL, 'F', 'sys:job:edit',    '#', '0', 0, NOW()),
(1204, '任务删除', 112, 4, '', NULL, 'F', 'sys:job:remove',  '#', '0', 0, NOW()),
(1205, '执行一次', 112, 5, '', NULL, 'F', 'sys:job:run',     '#', '0', 0, NOW()),
-- 文件管理按钮
(1301, '文件上传', 120, 1, '', NULL, 'F', 'sys:oss:upload',  '#', '0', 0, NOW()),
(1302, '文件下载', 120, 2, '', NULL, 'F', 'sys:oss:download','#', '0', 0, NOW()),
(1303, '文件删除', 120, 3, '', NULL, 'F', 'sys:oss:remove',  '#', '0', 0, NOW()),
-- OSS配置按钮
(1241, '配置查询', 124, 1, '', NULL, 'F', 'sys:oss-config:detail', '#', '0', 0, NOW()),
(1242, '配置新增', 124, 2, '', NULL, 'F', 'sys:oss-config:add',    '#', '0', 0, NOW()),
(1243, '配置修改', 124, 3, '', NULL, 'F', 'sys:oss-config:edit',   '#', '0', 0, NOW()),
(1244, '配置删除', 124, 4, '', NULL, 'F', 'sys:oss-config:remove', '#', '0', 0, NOW()),
-- 代码生成按钮
(1311, '生成预览', 131, 1, '', NULL, 'F', 'sys:gen:preview',  '#', '0', 0, NOW()),
(1312, '生成下载', 131, 2, '', NULL, 'F', 'sys:gen:download', '#', '0', 0, NOW());

-- 缓存监控补按钮：键删除
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, visible, status, create_time) VALUES
(132, '首页看板', 2, 7, 'dashboard', 'monitor/dashboard/index', 'C', 'sys:dashboard', 'dashboard', '0', 0, NOW()),
(1112, '键删除', 111, 2, '', NULL, 'F', 'sys:cache:remove', '#', '0', 0, NOW());

-- 调度任务示例目标 Bean 由应用内置（JobDemoTask），无需额外授权
