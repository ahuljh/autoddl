// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.database;

import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.common.util.DbswitchStrUtils;
import com.gitee.dbswitch.common.util.TypeConvertUtils;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.TableDescription;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据库元信息抽象基类
 *
 * @author tang
 */
public abstract class AbstractDatabase implements IDatabaseInterface {

  public static final int CLOB_LENGTH = 9999999;

  protected String driverClassName;
  protected String catalogName = null;

  public AbstractDatabase(String driverClassName) {
    try {
      this.driverClassName = driverClassName;
      Class.forName(driverClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getDriverClassName() {
    return this.driverClassName;
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    Set<String> ret = new HashSet<>();
    try (ResultSet schemas = connection.getMetaData().getSchemas()) {
      while (schemas.next()) {
        ret.add(schemas.getString("TABLE_SCHEM"));
      }
      return new ArrayList<>(ret);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> ret = new ArrayList<>();
    Set<String> uniqueSet = new HashSet<>();
    String[] types = new String[]{"TABLE", "VIEW"};
    try (ResultSet tables = connection.getMetaData()
        .getTables(this.catalogName, schemaName, "%", types)) {
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
        td.setTableType(tables.getString("TABLE_TYPE").toUpperCase());
        ret.add(td);
      }
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TableDescription queryTableMeta(Connection connection, String schemaName,
      String tableName) {
    return queryTableList(connection, schemaName).stream()
        .filter(one -> tableName.equals(one.getTableName()))
        .findAny().orElse(null);
  }

  @Override
  public List<String> queryTableColumnName(Connection connection, String schemaName,
      String tableName) {
    Set<String> columns = new HashSet<>();
    try (ResultSet rs = connection.getMetaData()
        .getColumns(this.catalogName, schemaName, tableName, null)) {
      while (rs.next()) {
        columns.add(rs.getString("COLUMN_NAME"));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return new ArrayList<>(columns);
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName) {
    String sql = this.getTableFieldsQuerySQL(schemaName, tableName);
    List<ColumnDescription> ret = this.querySelectSqlColumnMeta(connection, sql);

    // 补充一下注释信息
    try (ResultSet columns = connection.getMetaData()
        .getColumns(this.catalogName, schemaName, tableName, null)) {
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        String remarks = columns.getString("REMARKS");
        for (ColumnDescription cd : ret) {
          if (columnName.equals(cd.getFieldName())) {
            cd.setRemarks(remarks);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName,
      String tableName) {
    Set<String> ret = new HashSet<>();
    try (ResultSet primaryKeys = connection.getMetaData()
        .getPrimaryKeys(this.catalogName, schemaName, tableName)) {
      while (primaryKeys.next()) {
        String name = primaryKeys.getString("COLUMN_NAME");
        if (!ret.contains(name)) {
          ret.add(name);
        }
      }
      return new ArrayList<>(ret);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public SchemaTableData queryTableData(Connection connection, String schemaName, String tableName,
      int rowCount) {
    String fullTableName = getQuotedSchemaTableCombination(schemaName, tableName);
    String querySQL = String.format("SELECT * FROM %s ", fullTableName);
    SchemaTableData data = new SchemaTableData();
    data.setSchemaName(schemaName);
    data.setTableName(tableName);
    data.setColumns(new ArrayList<>());
    data.setRows(new ArrayList<>());
    try (Statement st = connection.createStatement()) {


      try (ResultSet rs = st.executeQuery(querySQL)) {
        ResultSetMetaData m = rs.getMetaData();
        int count = m.getColumnCount();
        for (int i = 1; i <= count; i++) {
          data.getColumns().add(m.getColumnLabel(i));
        }

        int counter = 0;
        while (rs.next() && counter++ < rowCount) {
          List<Object> row = new ArrayList<>(count);
          for (int i = 1; i <= count; i++) {
            Object value = rs.getObject(i);
            if (value != null && value instanceof byte[]) {
              row.add(DbswitchStrUtils.toHexString((byte[]) value));
            } else if (value != null && value instanceof java.sql.Clob) {
              row.add(TypeConvertUtils.castToString(value));
            } else if (value != null && value instanceof java.sql.Blob) {
              byte[] bytes = TypeConvertUtils.castToByteArray(value);
              row.add(DbswitchStrUtils.toHexString(bytes));
            } else {
              row.add(null == value ? null : value.toString());
            }
          }
          data.getRows().add(row);
        }

        return data;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {
    String wrapperSql = this.getTestQuerySQL(sql);
    try (Statement statement = connection.createStatement();) {
      statement.execute(wrapperSql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
    return String.format(" \"%s\".\"%s\" ", schemaName, tableName);
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr, boolean withRemarks) {
    throw new RuntimeException("AbstractDatabase Unimplemented!");
  }

  @Override
  public String getPrimaryKeyAsString(List<String> pks) {
    if (!pks.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("\"");
      sb.append(StringUtils.join(pks, "\" , \""));
      sb.append("\"");
      return sb.toString();
    }

    return "";
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td,
      List<ColumnDescription> cds) {
    throw new RuntimeException("AbstractDatabase Unimplemented!");
  }

  /**************************************
   * internal function
   **************************************/

  protected abstract String getTableFieldsQuerySQL(String schemaName, String tableName);

  protected abstract String getTestQuerySQL(String sql);

  protected List<ColumnDescription> getSelectSqlColumnMeta(Connection connection, String querySQL) {
    List<ColumnDescription> ret = new ArrayList<>();
    try (Statement st = connection.createStatement()) {

      try (ResultSet rs = st.executeQuery(querySQL)) {
        ResultSetMetaData m = rs.getMetaData();
        int columns = m.getColumnCount();
        for (int i = 1; i <= columns; i++) {
          String name = m.getColumnLabel(i);
          if (null == name) {
            name = m.getColumnName(i);
          }

          ColumnDescription cd = new ColumnDescription();
          cd.setFieldName(name);
          cd.setLabelName(name);
          cd.setFieldType(m.getColumnType(i));
          if (0 != cd.getFieldType()) {
            cd.setFieldTypeName(m.getColumnTypeName(i));
            cd.setFiledTypeClassName(m.getColumnClassName(i));
            cd.setDisplaySize(m.getColumnDisplaySize(i));
            cd.setPrecisionSize(m.getPrecision(i));
            cd.setScaleSize(m.getScale(i));
            cd.setAutoIncrement(m.isAutoIncrement(i));
            cd.setNullable(m.isNullable(i) != ResultSetMetaData.columnNoNulls);
          } else {
            // 处理视图中NULL as fieldName的情况
            cd.setFieldTypeName("CHAR");
            cd.setFiledTypeClassName(String.class.getName());
            cd.setDisplaySize(1);
            cd.setPrecisionSize(1);
            cd.setScaleSize(0);
            cd.setAutoIncrement(false);
            cd.setNullable(true);
          }

          boolean signed = false;
          try {
            signed = m.isSigned(i);
          } catch (Exception ignored) {
            // This JDBC Driver doesn't support the isSigned method
            // nothing more we can do here by catch the exception.
          }
          cd.setSigned(signed);
          cd.setDbType(getDatabaseType());

          ret.add(cd);
        }

        return ret;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}