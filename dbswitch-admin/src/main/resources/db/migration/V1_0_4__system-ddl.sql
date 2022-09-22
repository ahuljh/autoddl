ALTER TABLE `DBSWITCH_ASSIGNMENT_CONFIG`
ADD COLUMN `target_only_create` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否只建表' AFTER `target_drop_table`;
