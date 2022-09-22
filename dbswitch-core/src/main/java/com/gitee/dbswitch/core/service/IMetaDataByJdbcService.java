// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.service;

import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.SchemaTableMeta;
import com.gitee.dbswitch.core.model.TableDescription;
import java.util.List;

/**
 * 元信息获取接口定义
 *
 * @author tang
 */
public interface IMetaDataByJdbcService {

  /**
   * 获取数据库类型
   *
   * @return
   */
  ProductTypeEnum getDatabaseType();

  /**
   * 获取数据库的schema模式列表
   *
   * @param jdbcUrl  数据库连接的JDBC-URL
   * @param username 数据库连接的帐号
   * @param password 数据库连接的密码
   * @return
   */
  List<String> querySchemaList(String jdbcUrl, String username, String password);

  /**
   * 获取指定Schema下所有的表列表
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName 模式名称
   * @return
   */
  List<TableDescription> queryTableList(String jdbcUrl, String username, String password,
      String schemaName);

  /**
   * 获取物理表的DDL建表语句
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return
   */
  String getTableDDL(String jdbcUrl, String username, String password, String schemaName,
      String tableName);

  /**
   * 获取视图表的DDL建表语句
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return
   */
  String getViewDDL(String jdbcUrl, String username, String password, String schemaName,
      String tableName);

  /**
   * 获取指定schema.table的表结构字段信息
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName 模式名称
   * @param tableName  表或视图名称
   * @return
   */
  List<ColumnDescription> queryTableColumnMeta(String jdbcUrl, String username, String password,
      String schemaName, String tableName);

  /**
   * 获取指定SQL结构字段信息
   *
   * @param jdbcUrl  数据库连接的JDBC-URL
   * @param username 数据库连接的帐号
   * @param password 数据库连接的密码
   * @param querySql 查询的SQL语句
   * @return
   */
  List<ColumnDescription> querySqlColumnMeta(String jdbcUrl, String username, String password,
      String querySql);

  /**
   * 获取表的主键信息字段列表
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName Schema模式名称
   * @param tableName  Table表名称
   * @return
   */
  List<String> queryTablePrimaryKeys(String jdbcUrl, String username, String password,
      String schemaName,
      String tableName);

  /**
   * 测试数据库SQL查询
   *
   * @param jdbcUrl  数据库连接的JDBC-URL
   * @param username 数据库连接的帐号
   * @param password 数据库连接的密码
   * @param sql      待查询的SQL语句
   */
  void testQuerySQL(String jdbcUrl, String username, String password, String sql);

  /**
   * 获取(物理/视图)表的元数据
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName Schema模式名称
   * @param tableName  Table表名称
   * @return
   */
  SchemaTableMeta queryTableMeta(String jdbcUrl, String username, String password,
      String schemaName, String tableName);

  /**
   * 获取(物理/视图)表的数据内容
   *
   * @param jdbcUrl    数据库连接的JDBC-URL
   * @param username   数据库连接的帐号
   * @param password   数据库连接的密码
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param rowCount   记录总数
   * @return
   */
  SchemaTableData queryTableData(String jdbcUrl, String username, String password,
      String schemaName, String tableName, int rowCount);

  /**
   * 根据字段结构信息组装对应数据库的建表DDL语句
   *
   * @param type        目的数据库类型
   * @param fieldNames  字段结构信息
   * @param primaryKeys 主键字段信息
   * @param schemaName  模式名称
   * @param tableName   表名称
   * @param autoIncr    是否允许主键自增
   * @return 对应数据库的DDL建表语句
   */
  String getDDLCreateTableSQL(ProductTypeEnum type, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr);
}
