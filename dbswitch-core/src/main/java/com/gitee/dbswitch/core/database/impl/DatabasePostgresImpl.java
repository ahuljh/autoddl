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
import com.gitee.dbswitch.core.database.constant.PostgresqlConst;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.util.DDLFormatterUtils;
import com.gitee.dbswitch.core.util.PostgresUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * 支持PostgreSQL数据库的元信息实现
 *
 * @author tang
 */
public class DatabasePostgresImpl extends AbstractDatabase implements IDatabaseInterface {

  private static Set<String> systemSchemas = new HashSet<>();

  private static final String SHOW_CREATE_VIEW_SQL_1 =
      "SELECT pg_get_viewdef((select pg_class.relfilenode from pg_catalog.pg_class \n"
          + "join pg_catalog.pg_namespace on pg_class.relnamespace = pg_namespace.oid \n"
          + "where pg_namespace.nspname='%s' and pg_class.relname ='%s'),true) ";
  private static final String SHOW_CREATE_VIEW_SQL_2 =
      "select pg_get_viewdef('\"%s\".\"%s\"', true)";

  static {
    systemSchemas.add("pg_aoseg");
    systemSchemas.add("information_schema");
    systemSchemas.add("pg_catalog");
    systemSchemas.add("pg_bitmapindex");
  }

  public DatabasePostgresImpl() {
    super("org.postgresql.Driver");
  }

  @Override
  public ProductTypeEnum getDatabaseType() {
    return ProductTypeEnum.POSTGRESQL;
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    List<String> schemas = super.querySchemaList(connection);
    return schemas.stream()
        .filter(s -> !systemSchemas.contains(s))
        .collect(Collectors.toList());
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    String sql = PostgresqlConst.CREATE_TABLE_SQL_TPL
        .replace(PostgresqlConst.TPL_KEY_SCHEMA, schemaName)
        .replace(PostgresqlConst.TPL_KEY_TABLE, tableName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return DDLFormatterUtils.format(rs.getString(1));
          }
        }
      }
    } catch (SQLException e) {
      //throw new RuntimeException(e);
    }

    // 低版本的PostgreSQL的表的DDL获取方法
    return PostgresUtils.getTableDDL(connection, schemaName, tableName);
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_VIEW_SQL_1, schemaName, tableName);
    try (Statement st = connection.createStatement()) {
      try {
        if (st.execute(sql)) {
          try (ResultSet rs = st.getResultSet()) {
            if (rs != null && rs.next()) {
              return rs.getString(1);
            }
          }
        }
      } catch (SQLException se) {
        sql = String.format(SHOW_CREATE_VIEW_SQL_2, schemaName, tableName);
        if (st.execute(sql)) {
          try (ResultSet rs = st.getResultSet()) {
            if (rs != null && rs.next()) {
              return rs.getString(1);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(" %s LIMIT 0 ", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("SELECT * FROM \"%s\".\"%s\"  ", schemaName, tableName);
  }

  @Override
  protected String getTestQuerySQL(String sql) {
    return String.format("explain %s", sql.replace(";", ""));
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr, boolean withRemarks) {
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    int type = v.getType();

    String retval = " \"" + fieldname + "\"   ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
        retval += "TIMESTAMP";
        break;
      case ColumnMetaData.TYPE_TIME:
        retval += "TIME";
        break;
      case ColumnMetaData.TYPE_DATE:
        retval += "DATE";
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "BOOLEAN";
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          if (useAutoInc) {
            retval += "BIGSERIAL";
          } else {
            retval += "BIGINT";
          }
        } else {
          if (length > 0) {
            if (precision > 0 || length > 18) {
              if ((length + precision) > 0 && precision > 0) {
                // Numeric(Precision, Scale): Precision = total length; Scale = decimal places
                retval += "NUMERIC(" + (length + precision) + ", " + precision + ")";
              } else {
                retval += "DOUBLE PRECISION";
              }
            } else {
              if (length > 9) {
                retval += "BIGINT";
              } else {
                if (length < 5) {
                  retval += "SMALLINT";
                } else {
                  retval += "INTEGER";
                }
              }
            }

          } else {
            retval += "DOUBLE PRECISION";
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length < 1 || length >= AbstractDatabase.CLOB_LENGTH) {
          retval += "TEXT";
        } else {
          if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
            retval += "VARCHAR(" + length + ")";
          } else {
            retval += "TEXT";
          }
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "BYTEA";
        break;
      default:
        retval += "TEXT";
        break;
    }

    if (addCr) {
      retval += Const.CR;
    }

    return retval;
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
