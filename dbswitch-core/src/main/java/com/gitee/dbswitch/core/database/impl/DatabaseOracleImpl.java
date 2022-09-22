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

import com.gitee.dbswitch.common.constant.Const;
import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.IDatabaseInterface;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.TableDescription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * 支持Oracle数据库的元信息实现
 * <p>
 * 备注：
 * <p>
 * （1）Oracle12c安装教程：
 * <p>
 * 官方安装版：https://www.w3cschool.cn/oraclejc/oraclejc-vuqx2qqu.html
 * <p>
 * Docker版本：http://www.pianshen.com/article/4448142743/
 * <p>
 * https://www.cnblogs.com/Dev0ps/p/10676930.html
 * <p>
 * (2) Oracle的一个表里至多只能有一个字段为LONG类型
 *
 * @author tang
 */
public class DatabaseOracleImpl extends AbstractDatabase implements IDatabaseInterface {

  private static final String SHOW_CREATE_TABLE_SQL =
      "SELECT DBMS_METADATA.GET_DDL('TABLE','%s','%s') FROM DUAL ";
  private static final String SHOW_CREATE_VIEW_SQL =
      "SELECT DBMS_METADATA.GET_DDL('VIEW','%s','%s') FROM DUAL ";

  public DatabaseOracleImpl() {
    super("oracle.jdbc.driver.OracleDriver");
  }

  @Override
  public ProductTypeEnum getDatabaseType() {
    return ProductTypeEnum.ORACLE;
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> ret = new ArrayList<>();
    String sql = String.format("SELECT \"OWNER\",\"TABLE_NAME\",\"TABLE_TYPE\",\"COMMENTS\" "
        + "FROM all_tab_comments where \"OWNER\"='%s'", schemaName);
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();) {
      while (rs.next()) {
        TableDescription td = new TableDescription();
        td.setSchemaName(rs.getString("OWNER"));
        td.setTableName(rs.getString("TABLE_NAME"));
        td.setRemarks(rs.getString("COMMENTS"));
        String tableType = rs.getString("TABLE_TYPE").trim();
        if (tableType.equalsIgnoreCase("VIEW")) {
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
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_TABLE_SQL, tableName, schemaName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return rs.getString(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_VIEW_SQL, tableName, schemaName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return rs.getString(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName,
      String tableName) {
    // Oracle表的主键可以使用如下命令设置主键是否生效
    // 使主键失效：alter table tableName disable primary key;
    // 使主键恢复：alter table tableName enable primary key;
    Set<String> ret = new HashSet<>();
    String sql = String.format(
        "SELECT col.COLUMN_NAME FROM all_cons_columns col INNER JOIN all_constraints con \n"
            + "ON col.constraint_name=con.constraint_name AND col.OWNER =con.OWNER  AND col.TABLE_NAME =con.TABLE_NAME \n"
            + "WHERE con.constraint_type = 'P' and con.STATUS='ENABLED' and con.owner='%s' AND con.table_name='%s'",
        schemaName, tableName);
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
    ) {
      while (rs.next()) {
        ret.add(rs.getString("COLUMN_NAME"));
      }

      return new ArrayList<>(ret);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format("SELECT * from (%s) tmp where ROWNUM<=1 ",
        sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("SELECT * FROM \"%s\".\"%s\" ", schemaName, tableName);
  }

  @Override
  protected String getTestQuerySQL(String sql) {
    return String.format("explain plan for %s", sql.replace(";", ""));
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr, boolean withRemarks) {
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    StringBuilder retval = new StringBuilder(128);
    retval.append(" \"").append(fieldname).append("\"    ");

    int type = v.getType();
    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
      case ColumnMetaData.TYPE_TIME:
        retval.append("TIMESTAMP");
        break;
      case ColumnMetaData.TYPE_DATE:
        retval.append("DATE");
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval.append("NUMBER(1)");
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        retval.append("NUMBER");
        if (length > 0) {
          if (length > 38) {
            length = 38;
          }

          retval.append('(').append(length);
          if (precision > 0) {
            retval.append(", ").append(precision);
          }
          retval.append(')');
        }
        break;
      case ColumnMetaData.TYPE_INTEGER:
        retval.append("INTEGER");
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length >= AbstractDatabase.CLOB_LENGTH) {
          retval.append("CLOB");
        } else {
          if (length == 1) {
            retval.append("NVARCHAR2(1)");
          } else if (length > 0 && length < 2000) {
            // VARCHAR2(size)，size最大值为4000，单位是字节；而NVARCHAR2(size)，size最大值为2000，单位是字符
            retval.append("NVARCHAR2(").append(length).append(')');
          } else {
            retval.append("CLOB");// We don't know, so we just use the maximum...
          }
        }
        break;
      case ColumnMetaData.TYPE_BINARY: // the BLOB can contain binary data.
        retval.append("BLOB");
        break;
      default:
        retval.append("CLOB");
        break;
    }

    if (addCr) {
      retval.append(Const.CR);
    }

    return retval.toString();
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td,
      List<ColumnDescription> cds) {
    List<String> results = new ArrayList<>();
    if (StringUtils.isNotBlank(td.getRemarks())) {
      results.add(String
          .format("COMMENT ON TABLE \"%s\".\"%s\" IS '%s' ",
              td.getSchemaName(), td.getTableName(),
              td.getRemarks().replace("\"", "\\\"")));
    }

    for (ColumnDescription cd : cds) {
      if (StringUtils.isNotBlank(cd.getRemarks())) {
        results.add(String
            .format("COMMENT ON COLUMN \"%s\".\"%s\".\"%s\" IS '%s' ",
                td.getSchemaName(), td.getTableName(), cd.getFieldName(),
                cd.getRemarks().replace("\"", "\\\"")));
      }
    }

    return results;
  }

}
