ALTER TABLE `DBSWITCH_ASSIGNMENT_CONFIG`
CHANGE COLUMN `table_prefix` `table_name_map`  longtext NULL COMMENT '表名映射关系' AFTER `target_schema`,
ADD COLUMN `column_name_map`  longtext NULL COMMENT '字段名映射关系' AFTER `table_name_map`;

UPDATE `DBSWITCH_ASSIGNMENT_CONFIG`
SET table_name_map= CONCAT('[{"fromPattern":"^","toValue":"',table_name_map,'"}]')
WHERE table_name_map !='';
