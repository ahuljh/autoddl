// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database.impl;

import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 支持SQLServer2000数据库的元信息实现
 *
 * @author tang
 */
public class DatabaseSqlserver2000Impl extends DatabaseSqlserverImpl implements IDatabaseInterface {

  public DatabaseSqlserver2000Impl() {
    super("com.microsoft.jdbc.sqlserver.SQLServerDriver");
  }

  @Override
  public ProductTypeEnum getDatabaseType() {
    return ProductTypeEnum.SQLSERVER2000;
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> ret = new ArrayList<>();
    Set<String> uniqueSet = new HashSet<>();
    String[] types = new String[]{"TABLE", "VIEW"};
    try (ResultSet tables = connection.getMetaData()
        .getTables(this.catalogName, schemaName, "%", types);) {
      while (tables.next()) {
        String tableName = tables.getString("TABLE_NAME");
        if (uniqueSet.contains(tableName)) {
          continue;
        } else {
          uniqueSet.add(tableName);
        }

        TableDescription td = new TableDescription();
        td.setSchemaName(schemaName);
        td.setTableName(tableName);
        td.setRemarks(tables.getString("REMARKS"));
        if (tables.getString("TABLE_TYPE").equalsIgnoreCase("VIEW")) {
          td.setTableType("VIEW");
        } else {
          td.setTableType("TABLE");
        }
        ret.add(td);
      }
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName) {
    String sql = this.getTableFieldsQuerySQL(schemaName, tableName);
    List<ColumnDescription> ret = this.querySelectSqlColumnMeta(connection, sql);
    try (ResultSet columns = connection.getMetaData()
        .getColumns(this.catalogName, schemaName, tableName, null)) {
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        String remarks = columns.getString("REMARKS");
        for (ColumnDescription cd : ret) {
          if (columnName.equalsIgnoreCase(cd.getFieldName())) {
            cd.setRemarks(remarks);
          }
        }
      }
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
