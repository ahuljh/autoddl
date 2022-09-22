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
import com.gitee.dbswitch.core.util.JdbcUrlUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * 支持MySQL数据库的元信息实现
 *
 * @author tang
 */
public class DatabaseMysqlImpl extends AbstractDatabase implements IDatabaseInterface {

  private static final String SHOW_CREATE_TABLE_SQL = "SHOW CREATE TABLE `%s`.`%s` ";
  private static final String SHOW_CREATE_VIEW_SQL = "SHOW CREATE VIEW `%s`.`%s` ";

  public DatabaseMysqlImpl() {
    super("com.mysql.jdbc.Driver");
  }

  public DatabaseMysqlImpl(String driverClassName) {
    super(driverClassName);
  }

  @Override
  public ProductTypeEnum getDatabaseType() {
    return ProductTypeEnum.MYSQL;
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    try {
      String mysqlJdbcUrl = connection.getMetaData().getURL();
      Map<String, String> data = JdbcUrlUtils.findParamsByMySqlJdbcUrl(mysqlJdbcUrl);
      return Collections.singletonList(data.get("schema"));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> ret = new ArrayList<>();
    String sql = String.format("SELECT `TABLE_SCHEMA`,`TABLE_NAME`,`TABLE_TYPE`,`TABLE_COMMENT` "
        + "FROM `information_schema`.`TABLES` where `TABLE_SCHEMA`='%s'", schemaName);
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();) {
      while (rs.next()) {
        TableDescription td = new TableDescription();
        td.setSchemaName(rs.getString("TABLE_SCHEMA"));
        td.setTableName(rs.getString("TABLE_NAME"));
        td.setRemarks(rs.getString("TABLE_COMMENT"));
        String tableType = rs.getString("TABLE_TYPE");
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
    String sql = String.format(SHOW_CREATE_TABLE_SQL, schemaName, tableName);
    List<String> result = new ArrayList<>();
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null) {
            while (rs.next()) {
              String value = rs.getString(2);
              Optional.ofNullable(value).ifPresent(result::add);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return result.stream().findAny().orElse(null);
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_VIEW_SQL, schemaName, tableName);
    List<String> result = new ArrayList<>();
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null) {
            while (rs.next()) {
              String value = rs.getString(2);
              Optional.ofNullable(value).ifPresent(result::add);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return result.stream().findAny().orElse(null);
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(" %s LIMIT 0,1", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("SELECT * FROM `%s`.`%s` ", schemaName, tableName);
  }

  @Override
  protected String getTestQuerySQL(String sql) {
    return String.format("explain %s", sql.replace(";", ""));
  }

  @Override
  public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
    return String.format("  `%s`.`%s` ", schemaName, tableName);
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr, boolean withRemarks) {
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    int type = v.getType();

    String retval = " `" + fieldname + "`  ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
        retval += "DATETIME";
        break;
      case ColumnMetaData.TYPE_TIME:
        retval += "TIME";
        break;
      case ColumnMetaData.TYPE_DATE:
        retval += "DATE";
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "TINYINT";
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          if (useAutoInc) {
            retval += "BIGINT AUTO_INCREMENT NOT NULL";
          } else {
            retval += "BIGINT NOT NULL";
          }
        } else {
          // Integer values...
          if (precision == 0) {
            if (length > 9) {
              if (length < 19) {
                // can hold signed values between -9223372036854775808 and 9223372036854775807
                // 18 significant digits
                retval += "BIGINT";
              } else {
                retval += "DECIMAL(" + length + ")";
              }
            } else {
              retval += "INT";
            }
          } else {
            // Floating point values...
            if (length > 15) {
              retval += "DECIMAL(" + length;
              if (precision > 0) {
                retval += ", " + precision;
              }
              retval += ")";
            } else {
              // A double-precision floating-point number is accurate to approximately 15
              // decimal places.
              // http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html
              retval += "DOUBLE";
            }
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length > 0) {
          if (length == 1) {
            retval += "CHAR(1)";
          } else if (length < 256) {
            retval += "VARCHAR(" + length + ")";
          } else if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
            /*
             * MySQL5.6中varchar字段为主键时最大长度为254,例如如下的建表语句在MySQL5.7下能通过，但在MySQL5.6下无法通过：
             *	create table `t_test`(
             *	`key` varchar(1024) binary,
             *	`val` varchar(1024) binary,
             *	primary key(`key`)
             * );
             */
            retval += "VARCHAR(254) BINARY";
          } else if (length < 65536) {
            retval += "TEXT";
          } else if (length < 16777216) {
            retval += "MEDIUMTEXT";
          } else {
            retval += "LONGTEXT";
          }
        } else {
          retval += "TINYTEXT";
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "LONGBLOB";
        break;
      default:
        retval += " LONGTEXT";
        break;
    }

    if (withRemarks && StringUtils.isNotBlank(v.getRemarks())) {
      retval += String.format(" COMMENT '%s' ", v.getRemarks().replace("'", "\\'"));
    }

    if (addCr) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override
  public String getPrimaryKeyAsString(List<String> pks) {
    if (null != pks && !pks.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("`");
      sb.append(StringUtils.join(pks, "` , `"));
      sb.append("`");
      return sb.toString();
    }

    return "";
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td,
      List<ColumnDescription> cds) {
    return Collections.emptyList();
  }

}
