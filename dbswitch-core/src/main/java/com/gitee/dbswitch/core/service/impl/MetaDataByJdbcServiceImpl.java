// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.service.impl;

import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.SchemaTableMeta;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataByJdbcService;
import com.gitee.dbswitch.core.util.ConnectionUtils;
import com.gitee.dbswitch.core.util.GenerateSqlUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 使用JDBC连接串的元数据获取服务
 *
 * @author tang
 */
public class MetaDataByJdbcServiceImpl implements IMetaDataByJdbcService {

  protected ProductTypeEnum dbType;
  protected AbstractDatabase database;

  public MetaDataByJdbcServiceImpl(ProductTypeEnum type) {
    this.dbType = type;
    this.database = DatabaseFactory.getDatabaseInstance(type);
  }

  @Override
  public ProductTypeEnum getDatabaseType() {
    return this.dbType;
  }

  @Override
  public List<String> querySchemaList(String jdbcUrl, String username, String password) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.querySchemaList(connection);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<TableDescription> queryTableList(String jdbcUrl, String username, String password,
      String schemaName) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.queryTableList(connection, schemaName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getTableDDL(String jdbcUrl, String username, String password, String schemaName,
      String tableName) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.getTableDDL(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getViewDDL(String jdbcUrl, String username, String password, String schemaName,
      String tableName) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.getViewDDL(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(String jdbcUrl, String username,
      String password, String schemaName, String tableName) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.queryTableColumnMeta(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<ColumnDescription> querySqlColumnMeta(String jdbcUrl, String username,
      String password, String querySql) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.querySelectSqlColumnMeta(connection, querySql);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(String jdbcUrl, String username, String password,
      String schemaName, String tableName) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.queryTablePrimaryKeys(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public SchemaTableMeta queryTableMeta(String jdbcUrl, String username, String password,
      String schemaName, String tableName) {
    SchemaTableMeta tableMeta = new SchemaTableMeta();
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      TableDescription tableDesc = database.queryTableMeta(connection, schemaName, tableName);
      if (null == tableDesc) {
        throw new IllegalArgumentException("Table Or View Not Exist");
      }

      List<ColumnDescription> columns = database
          .queryTableColumnMeta(connection, schemaName, tableName);

      List<String> pks;
      String createSql;
      if (tableDesc.isViewTable()) {
        pks = Collections.emptyList();
        createSql = database.getViewDDL(connection, schemaName, tableName);
      } else {
        pks = database.queryTablePrimaryKeys(connection, schemaName, tableName);
        createSql = database.getTableDDL(connection, schemaName, tableName);
      }

      tableMeta.setSchemaName(schemaName);
      tableMeta.setTableName(tableName);
      tableMeta.setTableType(tableDesc.getTableType());
      tableMeta.setRemarks(tableDesc.getRemarks());
      tableMeta.setColumns(columns);
      tableMeta.setPrimaryKeys(pks);
      tableMeta.setCreateSql(createSql);

      return tableMeta;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public SchemaTableData queryTableData(String jdbcUrl, String username, String password,
      String schemaName, String tableName, int rowCount) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      return database.queryTableData(connection, schemaName, tableName, rowCount);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public void testQuerySQL(String jdbcUrl, String username, String password, String sql) {
    try (Connection connection = ConnectionUtils.connect(jdbcUrl, username, password)) {
      database.testQuerySQL(connection, sql);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getDDLCreateTableSQL(ProductTypeEnum type, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, boolean autoIncr) {
    return GenerateSqlUtils.getDDLCreateTableSQL(
        type, fieldNames, primaryKeys, schemaName, tableName, autoIncr);
  }
}
