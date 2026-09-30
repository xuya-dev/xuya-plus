-- ==========================================================================
-- XuYa Plus 数据库初始化脚本（RuoYi-Vue-Plus 同款系统表，去除多租户）
-- 适用：MySQL 8.x / 5.7+，utf8mb4
-- 使用：mysql -uroot -p < xuya_plus.sql
-- 默认账号：admin / admin123（超级管理员）；test / admin123（普通角色，演示数据权限）
-- ==========================================================================

CREATE DATABASE IF NOT EXISTS xuya_plus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE xuya_plus;

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    parent_id   BIGINT       DEFAULT 0               COMMENT '父部门ID',
    ancestors   VARCHAR(50)  DEFAULT ''              COMMENT '祖级路径（0,100,101）',
    dept_name   VARCHAR(30)  DEFAULT ''              COMMENT '部门名称',
    order_num   INT          DEFAULT 0               COMMENT '显示顺序',
    leader      VARCHAR(20)  DEFAULT NULL            COMMENT '负责人',
    phone       VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
    email       VARCHAR(50)  DEFAULT NULL            COMMENT '邮箱',
    status      TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    del_flag    TINYINT      DEFAULT 0               COMMENT '删除标志（0存在 2删除）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    dept_id     BIGINT       DEFAULT NULL            COMMENT '部门ID',
    username    VARCHAR(30)  NOT NULL                COMMENT '用户账号',
    nickname    VARCHAR(30)  DEFAULT ''              COMMENT '用户昵称',
    password    VARCHAR(100) DEFAULT ''              COMMENT '密码（BCrypt）',
    email       VARCHAR(50)  DEFAULT ''              COMMENT '邮箱',
    phone       VARCHAR(20)  DEFAULT ''              COMMENT '手机号',
    sex         CHAR(1)      DEFAULT '2'             COMMENT '性别（0男 1女 2未知）',
    avatar      VARCHAR(255) DEFAULT ''              COMMENT '头像地址',
    status      TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    login_ip    VARCHAR(128) DEFAULT ''              COMMENT '最后登录IP',
    login_date  DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    del_flag    TINYINT      DEFAULT 0               COMMENT '删除标志（0存在 2删除）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dept_id (dept_id),
    KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 岗位表
-- ----------------------------
DROP TABLE IF EXISTS sys_post;
CREATE TABLE sys_post (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
    post_code   VARCHAR(64)  NOT NULL                COMMENT '岗位编码',
    post_name   VARCHAR(50)  NOT NULL                COMMENT '岗位名称',
    post_sort   INT          DEFAULT 0               COMMENT '显示顺序',
    status      TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    del_flag    TINYINT      DEFAULT 0               COMMENT '删除标志（0存在 2删除）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name           VARCHAR(30)  NOT NULL                COMMENT '角色名称',
    role_key            VARCHAR(100) NOT NULL                COMMENT '角色权限字符串',
    role_sort           INT          DEFAULT 0               COMMENT '显示顺序',
    data_scope          TINYINT      DEFAULT 1               COMMENT '数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）',
    menu_check_strictly TINYINT(1)   DEFAULT 1               COMMENT '菜单树选择项是否关联显示',
    dept_check_strictly TINYINT(1)   DEFAULT 1               COMMENT '部门树选择项是否关联显示',
    status              TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    del_flag            TINYINT      DEFAULT 0               COMMENT '删除标志（0存在 2删除）',
    remark              VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    create_by           VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time         DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by           VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time         DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 菜单权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    menu_name   VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
    parent_id   BIGINT       DEFAULT 0               COMMENT '父菜单ID',
    order_num   INT          DEFAULT 0               COMMENT '显示顺序',
    path        VARCHAR(200) DEFAULT ''              COMMENT '路由地址',
    component   VARCHAR(255) DEFAULT NULL            COMMENT '组件路径',
    menu_type   CHAR(1)      DEFAULT ''              COMMENT '类型（M目录 C菜单 F按钮）',
    perms       VARCHAR(100) DEFAULT NULL            COMMENT '权限标识（如 sys:user:add）',
    icon        VARCHAR(100) DEFAULT '#'             COMMENT '菜单图标',
    visible     CHAR(1)      DEFAULT '0'             COMMENT '显示状态（0显示 1隐藏）',
    status      TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    remark      VARCHAR(255) DEFAULT ''              COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ----------------------------
-- 用户角色 / 用户岗位 / 角色菜单 / 角色部门 关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

DROP TABLE IF EXISTS sys_user_post;
CREATE TABLE sys_user_post (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '岗位ID',
    PRIMARY KEY (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和岗位关联表';

DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

DROP TABLE IF EXISTS sys_role_dept;
CREATE TABLE sys_role_dept (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和部门关联表';

-- ----------------------------
-- 字典类型 / 字典数据
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典主键',
    dict_name   VARCHAR(100) DEFAULT ''              COMMENT '字典名称',
    dict_type   VARCHAR(100) DEFAULT ''              COMMENT '字典类型（唯一）',
    status      TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    del_flag    TINYINT      DEFAULT 0               COMMENT '删除标志（0存在 2删除）',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典编码',
    dict_type   VARCHAR(100) DEFAULT ''              COMMENT '字典类型',
    dict_label  VARCHAR(100) DEFAULT ''              COMMENT '数据标签',
    dict_value  VARCHAR(100) DEFAULT ''              COMMENT '数据键值',
    dict_sort   INT          DEFAULT 0               COMMENT '显示顺序',
    is_default  CHAR(1)      DEFAULT 'N'             COMMENT '是否默认（Y是 N否）',
    status      TINYINT      DEFAULT 0               COMMENT '状态（0正常 1停用）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    del_flag    TINYINT      DEFAULT 0               COMMENT '删除标志（0存在 2删除）',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- 参数配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '参数主键',
    config_name VARCHAR(100) DEFAULT ''              COMMENT '参数名称',
    config_key  VARCHAR(100) DEFAULT ''              COMMENT '参数键名',
    config_value VARCHAR(500) DEFAULT ''             COMMENT '参数键值',
    config_type CHAR(1)      DEFAULT 'N'             COMMENT '系统内置（Y是 N否）',
    remark      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- ----------------------------
-- 通知公告表
-- ----------------------------
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    notice_title   VARCHAR(100) NOT NULL                COMMENT '公告标题',
    notice_type    TINYINT      DEFAULT 1               COMMENT '公告类型（1通知 2公告）',
    notice_content LONGTEXT                             COMMENT '公告内容',
    status         TINYINT      DEFAULT 0               COMMENT '状态（0正常 1关闭）',
    remark         VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    create_by      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ----------------------------
-- 操作日志 / 登录日志
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    module       VARCHAR(50)  DEFAULT ''              COMMENT '业务模块',
    description  VARCHAR(100) DEFAULT ''              COMMENT '操作描述',
    operator     VARCHAR(64)  DEFAULT ''              COMMENT '操作人（loginId）',
    uri          VARCHAR(255) DEFAULT ''              COMMENT '请求URI',
    http_method  VARCHAR(10)  DEFAULT ''              COMMENT 'HTTP方法',
    ip           VARCHAR(128) DEFAULT ''              COMMENT '客户端IP',
    params       TEXT                                 COMMENT '入参JSON',
    result_code  INT          DEFAULT NULL            COMMENT '响应业务码',
    status       TINYINT      DEFAULT 0               COMMENT '状态（0成功 1失败）',
    error_msg    TEXT                                 COMMENT '异常信息',
    cost_ms      BIGINT       DEFAULT 0               COMMENT '耗时毫秒',
    oper_time    DATETIME     DEFAULT NULL            COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_oper_time (oper_time),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

DROP TABLE IF EXISTS sys_logininfor;
CREATE TABLE sys_logininfor (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '访问ID',
    username   VARCHAR(50)  DEFAULT ''              COMMENT '登录账号',
    ip         VARCHAR(128) DEFAULT ''              COMMENT '登录IP',
    msg        VARCHAR(255) DEFAULT ''              COMMENT '日志内容',
    status     TINYINT      DEFAULT 0               COMMENT '状态（0成功 1失败）',
    login_time DATETIME     DEFAULT NULL            COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统访问（登录日志）表';

-- ==========================================================================
-- 种子数据
-- ==========================================================================

-- 部门（RuoYi 结构）
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, order_num, leader, status, create_time) VALUES
(100, 0,   '0',        'Xuya科技',   0, '徐崖', 0, NOW()),
(101, 100, '0,100',    '深圳总公司', 1, '徐崖', 0, NOW()),
(102, 100, '0,100',    '长沙分公司', 2, '徐崖', 0, NOW()),
(103, 101, '0,100,101','研发部门',   1, '徐崖', 0, NOW()),
(104, 101, '0,100,101','市场部门',   2, '徐崖', 0, NOW()),
(105, 101, '0,100,101','测试部门',   3, '徐崖', 0, NOW()),
(106, 101, '0,100,101','财务部门',   4, '徐崖', 0, NOW()),
(107, 101, '0,100,101','运维部门',   5, '徐崖', 0, NOW()),
(108, 102, '0,100,102','市场部门',   1, '徐崖', 0, NOW()),
(109, 102, '0,100,102','财务部门',   2, '徐崖', 0, NOW());

-- 用户（admin/test 密码均为 admin123 的 BCrypt 密文）
INSERT INTO sys_user (id, dept_id, username, nickname, password, email, phone, sex, status, remark, create_time) VALUES
(1, 103, 'admin', '超级管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'xuya_dev@qq.com', '15888888888', '2', 0, '内置超级管理员，请及时修改密码', NOW()),
(2, 105, 'test',  '测试用户',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'test@qq.com',     '15666666666', '1', 0, '普通角色演示账号', NOW());

-- 岗位
INSERT INTO sys_post (id, post_code, post_name, post_sort, status, create_time) VALUES
(1, 'ceo',  '董事长',   1, 0, NOW()),
(2, 'se',   '项目经理', 2, 0, NOW()),
(3, 'hr',   '人力资源', 3, 0, NOW()),
(4, 'user', '程序员',   4, 0, NOW()),
(5, 'der',  '运维',     5, 0, NOW());

-- 角色
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, remark, create_time) VALUES
(1, '超级管理员', 'admin',  1, 1, 0, '内置角色，拥有全部权限（*:*:*）', NOW()),
(2, '普通角色',   'common', 2, 4, 0, '演示：本部门及以下数据权限', NOW());

-- 菜单（目录 / 菜单）
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, visible, status, create_time) VALUES
(1,   '系统管理', 0, 1, 'system',    NULL,                     'M', NULL, 'system',    '0', 0, NOW()),
(2,   '系统监控', 0, 2, 'monitor',   NULL,                     'M', NULL, 'monitor',   '0', 0, NOW()),
(100, '用户管理', 1, 1, 'user',      'system/user/index',      'C', 'sys:user:list',      'user',        '0', 0, NOW()),
(101, '角色管理', 1, 2, 'role',      'system/role/index',      'C', 'sys:role:list',      'peoples',     '0', 0, NOW()),
(102, '菜单管理', 1, 3, 'menu',      'system/menu/index',      'C', 'sys:menu:list',      'tree-table',  '0', 0, NOW()),
(103, '部门管理', 1, 4, 'dept',      'system/dept/index',      'C', 'sys:dept:list',      'tree',        '0', 0, NOW()),
(104, '岗位管理', 1, 5, 'post',      'system/post/index',      'C', 'sys:post:list',      'post',        '0', 0, NOW()),
(105, '字典管理', 1, 6, 'dict',      'system/dict/index',      'C', 'sys:dict:list',      'dict',        '0', 0, NOW()),
(106, '参数设置', 1, 7, 'config',    'system/config/index',    'C', 'sys:config:list',    'edit',        '0', 0, NOW()),
(107, '通知公告', 1, 8, 'notice',    'system/notice/index',    'C', 'sys:notice:list',    'message',     '0', 0, NOW()),
(108, '操作日志', 2, 1, 'operlog',   'monitor/operlog/index',  'C', 'sys:operlog:list',   'form',        '0', 0, NOW()),
(109, '登录日志', 2, 2, 'logininfor','monitor/logininfor/index','C','sys:logininfor:list','logininfor',  '0', 0, NOW()),
(110, '在线用户', 2, 3, 'online',    'monitor/online/index',   'C', 'sys:online:list',    'online',      '0', 0, NOW()),
(111, '缓存监控', 2, 4, 'cache',     'monitor/cache/index',    'C', 'sys:cache:list',     'redis',       '0', 0, NOW());

-- 菜单（按钮：权限码与 Quick-Dev 约定一致 前缀:操作）
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, visible, status, create_time) VALUES
-- 用户管理
(1001, '用户详情', 100, 1, '', NULL, 'F', 'sys:user:detail', '#', '0', 0, NOW()),
(1002, '用户新增', 100, 2, '', NULL, 'F', 'sys:user:add',    '#', '0', 0, NOW()),
(1003, '用户修改', 100, 3, '', NULL, 'F', 'sys:user:edit',   '#', '0', 0, NOW()),
(1004, '用户删除', 100, 4, '', NULL, 'F', 'sys:user:remove', '#', '0', 0, NOW()),
(1005, '用户导出', 100, 5, '', NULL, 'F', 'sys:user:export', '#', '0', 0, NOW()),
-- 角色管理
(1011, '角色详情', 101, 1, '', NULL, 'F', 'sys:role:detail', '#', '0', 0, NOW()),
(1012, '角色新增', 101, 2, '', NULL, 'F', 'sys:role:add',    '#', '0', 0, NOW()),
(1013, '角色修改', 101, 3, '', NULL, 'F', 'sys:role:edit',   '#', '0', 0, NOW()),
(1014, '角色删除', 101, 4, '', NULL, 'F', 'sys:role:remove', '#', '0', 0, NOW()),
(1015, '角色导出', 101, 5, '', NULL, 'F', 'sys:role:export', '#', '0', 0, NOW()),
-- 菜单管理
(1021, '菜单详情', 102, 1, '', NULL, 'F', 'sys:menu:detail', '#', '0', 0, NOW()),
(1022, '菜单新增', 102, 2, '', NULL, 'F', 'sys:menu:add',    '#', '0', 0, NOW()),
(1023, '菜单修改', 102, 3, '', NULL, 'F', 'sys:menu:edit',   '#', '0', 0, NOW()),
(1024, '菜单删除', 102, 4, '', NULL, 'F', 'sys:menu:remove', '#', '0', 0, NOW()),
-- 部门管理
(1031, '部门详情', 103, 1, '', NULL, 'F', 'sys:dept:detail', '#', '0', 0, NOW()),
(1032, '部门新增', 103, 2, '', NULL, 'F', 'sys:dept:add',    '#', '0', 0, NOW()),
(1033, '部门修改', 103, 3, '', NULL, 'F', 'sys:dept:edit',   '#', '0', 0, NOW()),
(1034, '部门删除', 103, 4, '', NULL, 'F', 'sys:dept:remove', '#', '0', 0, NOW()),
-- 岗位管理
(1041, '岗位详情', 104, 1, '', NULL, 'F', 'sys:post:detail', '#', '0', 0, NOW()),
(1042, '岗位新增', 104, 2, '', NULL, 'F', 'sys:post:add',    '#', '0', 0, NOW()),
(1043, '岗位修改', 104, 3, '', NULL, 'F', 'sys:post:edit',   '#', '0', 0, NOW()),
(1044, '岗位删除', 104, 4, '', NULL, 'F', 'sys:post:remove', '#', '0', 0, NOW()),
(1045, '岗位导出', 104, 5, '', NULL, 'F', 'sys:post:export', '#', '0', 0, NOW()),
-- 字典管理
(1051, '字典详情', 105, 1, '', NULL, 'F', 'sys:dict:detail', '#', '0', 0, NOW()),
(1052, '字典新增', 105, 2, '', NULL, 'F', 'sys:dict:add',    '#', '0', 0, NOW()),
(1053, '字典修改', 105, 3, '', NULL, 'F', 'sys:dict:edit',   '#', '0', 0, NOW()),
(1054, '字典删除', 105, 4, '', NULL, 'F', 'sys:dict:remove', '#', '0', 0, NOW()),
(1055, '字典刷新', 105, 5, '', NULL, 'F', 'dict:refresh',    '#', '0', 0, NOW()),
-- 参数设置
(1061, '参数详情', 106, 1, '', NULL, 'F', 'sys:config:detail', '#', '0', 0, NOW()),
(1062, '参数新增', 106, 2, '', NULL, 'F', 'sys:config:add',    '#', '0', 0, NOW()),
(1063, '参数修改', 106, 3, '', NULL, 'F', 'sys:config:edit',   '#', '0', 0, NOW()),
(1064, '参数删除', 106, 4, '', NULL, 'F', 'sys:config:remove', '#', '0', 0, NOW()),
-- 通知公告
(1071, '公告详情', 107, 1, '', NULL, 'F', 'sys:notice:detail', '#', '0', 0, NOW()),
(1072, '公告新增', 107, 2, '', NULL, 'F', 'sys:notice:add',    '#', '0', 0, NOW()),
(1073, '公告修改', 107, 3, '', NULL, 'F', 'sys:notice:edit',   '#', '0', 0, NOW()),
(1074, '公告删除', 107, 4, '', NULL, 'F', 'sys:notice:remove', '#', '0', 0, NOW()),
-- 操作日志
(1081, '日志删除', 108, 1, '', NULL, 'F', 'sys:operlog:remove', '#', '0', 0, NOW()),
(1082, '日志清空', 108, 2, '', NULL, 'F', 'sys:operlog:clean',  '#', '0', 0, NOW()),
-- 登录日志
(1091, '日志删除', 109, 1, '', NULL, 'F', 'sys:logininfor:remove', '#', '0', 0, NOW()),
(1092, '日志清空', 109, 2, '', NULL, 'F', 'sys:logininfor:clean',  '#', '0', 0, NOW()),
-- 在线用户
(1101, '强退',     110, 1, '', NULL, 'F', 'sys:online:kick',   '#', '0', 0, NOW()),
-- 缓存监控
(1111, '刷新缓存', 111, 1, '', NULL, 'F', 'sys:cache:refresh', '#', '0', 0, NOW());

-- 用户-角色 / 用户-岗位
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2);
INSERT INTO sys_user_post (user_id, post_id) VALUES (1, 1), (2, 4);

-- 角色-菜单：普通角色只授予系统管理目录 + 用户查看（演示权限裁剪）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1), (2, 100);

-- 字典类型
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark, create_time) VALUES
(1, '系统开关', 'sys_normal_disable', 0, '系统通用开关（0正常 1停用）', NOW()),
(2, '用户性别', 'sys_user_sex',       0, '用户性别列表', NOW()),
(3, '通知类型', 'sys_notice_type',    0, '通知类型列表', NOW()),
(4, '通知状态', 'sys_notice_status',  0, '通知状态列表', NOW()),
(5, '系统是否', 'sys_yes_no',         0, '系统是否列表', NOW()),
(6, '数据范围', 'sys_data_scope',     0, '角色数据范围', NOW());

-- 字典数据
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_time) VALUES
(1,  'sys_normal_disable', '正常', '0', 1, 'Y', 0, NOW()),
(2,  'sys_normal_disable', '停用', '1', 2, 'N', 0, NOW()),
(3,  'sys_user_sex',       '男',   '0', 1, 'Y', 0, NOW()),
(4,  'sys_user_sex',       '女',   '1', 2, 'N', 0, NOW()),
(5,  'sys_user_sex',       '未知', '2', 3, 'N', 0, NOW()),
(6,  'sys_notice_type',    '通知', '1', 1, 'Y', 0, NOW()),
(7,  'sys_notice_type',    '公告', '2', 2, 'N', 0, NOW()),
(8,  'sys_notice_status',  '正常', '0', 1, 'Y', 0, NOW()),
(9,  'sys_notice_status',  '关闭', '1', 2, 'N', 0, NOW()),
(10, 'sys_yes_no',         '是',   'Y', 1, 'Y', 0, NOW()),
(11, 'sys_yes_no',         '否',   'N', 2, 'N', 0, NOW()),
(12, 'sys_data_scope',     '全部数据权限',       '1', 1, 'Y', 0, NOW()),
(13, 'sys_data_scope',     '自定义数据权限',     '2', 2, 'N', 0, NOW()),
(14, 'sys_data_scope',     '本部门数据权限',     '3', 3, 'N', 0, NOW()),
(15, 'sys_data_scope',     '本部门及以下数据权限','4', 4, 'N', 0, NOW()),
(16, 'sys_data_scope',     '仅本人数据权限',     '5', 5, 'N', 0, NOW());

-- 参数配置（验证码开关：运行时可在"参数设置"里改，立即生效）
INSERT INTO sys_config (id, config_name, config_key, config_value, config_type, remark, create_time) VALUES
(1, '是否开启验证码', 'sys.account.captchaEnabled', 'true', 'Y', '登录页验证码开关（true/false）', NOW());

-- 通知公告
INSERT INTO sys_notice (id, notice_title, notice_type, notice_content, status, create_time) VALUES
(1, '欢迎使用 XuYa Plus 后台管理脚手架', 1, '<p>默认账号 admin / admin123，基于 Quick-Dev 注解式 CRUD 框架构建。</p>', 0, NOW()),
(2, '关于数据权限的说明', 2, '<p>普通角色默认"本部门及以下"数据权限，可在角色管理中调整 data_scope。</p>', 0, NOW());
