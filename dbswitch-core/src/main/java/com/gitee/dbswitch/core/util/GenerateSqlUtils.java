// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.util;

import com.gitee.dbswitch.common.constant.Const;
import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.core.database.AbstractDatabase;
import com.gitee.dbswitch.core.database.DatabaseFactory;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.ColumnMetaData;
import com.gitee.dbswitch.core.model.TableDescription;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * 拼接SQL工具类
 *
 * @author tang
 */
public final class GenerateSqlUtils {

  public static String getDDLCreateTableSQL(
      ProductTypeEnum type,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      boolean autoIncr) {
    AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);
    return getDDLCreateTableSQL(
        db,
        fieldNames,
        primaryKeys,
        schemaName,
        tableName,
        false,
        null,
        autoIncr);
  }

  public static String getDDLCreateTableSQL(
      AbstractDatabase db,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      boolean withRemarks,
      String tableRemarks,
      boolean autoIncr) {
    ProductTypeEnum type = db.getDatabaseType();
    StringBuilder sb = new StringBuilder();
    List<String> pks = fieldNames.stream()
        .filter((cd) -> primaryKeys.contains(cd.getFieldName()))
        .map((cd) -> cd.getFieldName())
        .collect(Collectors.toList());

    sb.append(Const.CREATE_TABLE);
    // if(ifNotExist && type!=DatabaseType.ORACLE) {
    // sb.append( Const.IF_NOT_EXISTS );
    // }
    sb.append(db.getQuotedSchemaTableCombination(schemaName, tableName));
    sb.append("(");

    for (int i = 0; i < fieldNames.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      } else {
        sb.append("  ");
      }

      ColumnMetaData v = fieldNames.get(i).getMetaData();
      sb.append(db.getFieldDefinition(v, pks, autoIncr, false, withRemarks));
    }

    if (!pks.isEmpty()) {
      String pk = db.getPrimaryKeyAsString(pks);
      sb.append(", PRIMARY KEY (").append(pk).append(")");
    }

    sb.append(")");
    if (ProductTypeEnum.MYSQL == type) {
      sb.append("ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
      if (withRemarks && StringUtils.isNotBlank(tableRemarks)) {
        sb.append(String.format(" COMMENT='%s' ", tableRemarks.replace("'", "\\'")));
      }
    }

    return DDLFormatterUtils.format(sb.toString());
  }

  public static List<String> getDDLCreateTableSQL(
      ProductTypeEnum type,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      String tableRemarks,
      boolean autoIncr) {
    AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);
    String createTableSql = getDDLCreateTableSQL(db, fieldNames, primaryKeys, schemaName,
        tableName, true, tableRemarks, autoIncr);
    if (type.noCommentStatement()) {
      return Arrays.asList(createTableSql);
    }

    TableDescription td = new TableDescription();
    td.setSchemaName(schemaName);
    td.setTableName(tableName);
    td.setRemarks(tableRemarks);
    td.setTableType("TABLE");
    List<String> results = db.getTableColumnCommentDefinition(td, fieldNames);
    results.add(0, createTableSql);
    return results;
  }

  private GenerateSqlUtils() {
    throw new IllegalStateException();
  }

}
