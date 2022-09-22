create table `DBSWITCH_SYSTEM_USER`  (
  `id`                  bigint(20)            not null auto_increment   comment '主键id',
  `username`            varchar(255)          not null                  comment '登录名称',
  `password`            varchar(128)          not null                  comment '登录密码',
  `salt`                varchar(128)          not null                  comment '密码盐值',
  `real_name`           varchar(255)          not null default ''       comment '实际姓名',
  `email`               varchar(255)          not null default ''       comment '电子邮箱',
  `address`             varchar(255)          not null default ''       comment '所在地址',
  `locked`              tinyint(1)            not null default 0        comment '是否锁定',
  `create_time`         timestamp             not null default current_timestamp comment '创建时间',
  `update_time`         timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`),
  unique key (`username`)
) engine = innodb auto_increment = 1 character set = utf8 comment = '系统用户表';

create table `DBSWITCH_SYSTEM_LOG` (
  `id`                  bigint(20)   unsigned not null auto_increment            comment '主键',
  `type`                smallint              not null default 0                 comment '日志类型:0-访问日志;1-操作日志',
  `username`            varchar(64)           not null default ''                comment '操作用户',
  `ip_address`          varchar(64)           not null default ''                comment '客户端ip',
  `module_name`         varchar(64)           not null default ''                comment '模块名',
  `content`             text                                                     comment '日志描述',
  `url_path`            varchar(64)           not null default ''                comment 'path路径',
  `user_agent`          varchar(255)          not null default ''                comment '客户端agent',
  `failed`              tinyint(1)            not null default 0                 comment '是否异常(0:否 1:是)',
  `exception`           longtext                                                 comment '异常堆栈信息',
  `elapse_seconds`      bigint(20)   unsigned not null default 0                 comment '执行时间（单位毫秒）',
  `create_time`         timestamp             not null default current_timestamp comment '创建时间',
primary key (`id`)
) engine=innodb auto_increment=1 default charset=utf8 comment='操作日志';

create table `DBSWITCH_DATABASE_CONNECTION` (
  `id`                  bigint(20)   unsigned not null auto_increment            comment '主键',
  `name`                varchar(200)          not null default ''                comment '连接名称',
  `type`                varchar(200)          not null default ''                comment '数据库类型',
  `driver`              varchar(200)          not null default ''                comment '驱动类名称',
  `url`                 longtext                                                 comment 'jdbc-url连接串',
  `username`            varchar(200)          not null default ''                comment '连接账号',
  `password`            varchar(200)          not null default ''                comment '账号密码',
  `create_time`         timestamp             not null default current_timestamp comment '创建时间',
  `update_time`         timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`),
  unique key (`name`)
) engine=innodb auto_increment=1 default charset=utf8 comment='数据库连接';

create table `DBSWITCH_ASSIGNMENT_TASK` (
  `id`              bigint(20)   unsigned not null auto_increment comment '主键',
  `name`            varchar(200)          not null default ''     comment '任务名称',
  `description`     text                                          comment '任务描述',
  `schedule_mode`   varchar(50)           null default null       comment '调度方式(cron/无调度)',
  `cron_expression` varchar(200)          not null default ''     comment '调度cron表达式',
  `published`       tinyint(1)            not null default 0      comment '是否已发布(0:否 1:是)',
  `content`         longtext                                      comment '发布的配置JSON格式',
  `create_time`     timestamp             not null default current_timestamp comment '创建时间',
  `update_time`     timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`)
) engine=innodb auto_increment=1 default charset=utf8 comment='任务信息表';

create table `DBSWITCH_ASSIGNMENT_CONFIG` (
  `id`                          bigint(20)   unsigned not null auto_increment comment '主键',
  `assignment_id`               bigint(20)   unsigned not null                comment '任务ID',
  `source_connection_id`        bigint(20)   unsigned not null                comment '来源端连接ID',
  `source_schema`               varchar(1024)         not null default ''     comment '来源端的schema',
  `source_tables`               longtext                                      comment '来源端的table列表',
  `excluded`                    tinyint(1)            not null default 0      comment '是否排除(0:否 1:是)',
  `target_connection_id`        bigint(20)   unsigned not null                comment '目的端连接ID',
  `target_schema`               varchar(200)          not null default ''     comment '目的端的schema(一个)',
  `table_prefix`                varchar(200)          not null default ''     comment '生成的目的地表前缀',
  `target_drop_table`           tinyint(1)            not null default 0      comment '同步前是否先删除目的表(0:否 1:是)',
  `batch_size`                  bigint(20)   unsigned not null default 10000  comment '处理批次大小',
  `first_flag`                  tinyint(1)            not null default 1      comment '首次加载数据',
  `create_time`                 timestamp             not null default current_timestamp comment '创建时间',
  primary key (`id`),
  unique key (`assignment_id`),
  foreign key (`assignment_id`) references `DBSWITCH_ASSIGNMENT_TASK` (`id`) on delete cascade on update cascade,
  foreign key (`source_connection_id`) references `DBSWITCH_DATABASE_CONNECTION` (`id`) on delete cascade on update cascade,
  foreign key (`target_connection_id`) references `DBSWITCH_DATABASE_CONNECTION` (`id`) on delete cascade on update cascade
) engine=innodb auto_increment=1 default charset=utf8 comment='任务配置表';

create table `DBSWITCH_ASSIGNMENT_JOB` (
  `id`                      bigint(20)   unsigned not null auto_increment            comment '主键',
  `assignment_id`           bigint(20)   unsigned not null default 0                 comment '任务ID',
  `job_key`                 varchar(200)          not null default ''                comment 'Quartz的Job名',
  `start_time`              timestamp             not null default current_timestamp comment '执行开始时间',
  `finish_time`             timestamp             not null default current_timestamp comment '执行结束时间',
  `schedule_mode`           smallint              not null default 0                 comment '调度模式',
  `status`                  smallint              not null default 0                 comment '执行状态:0-未执行;1-执行中;2-执行失败;3-执行成功',
  `error_log`               longtext                                                 comment '异常日志',
  `create_time`             timestamp             not null default current_timestamp comment '创建时间',
  `update_time`             timestamp             not null default current_timestamp on update current_timestamp comment '修改时间',
  primary key (`id`),
  foreign key (`assignment_id`) references `DBSWITCH_ASSIGNMENT_TASK` (`id`) on delete cascade on update cascade
) engine=innodb auto_increment=1 default charset=utf8 comment='JOB日志表';
