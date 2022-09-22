// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.core.model;

import com.gitee.dbswitch.common.type.DBTableType;

/**
 * 数据库表描述符信息定义(Table Description)
 *
 * @author tang
 */
public class TableDescription {

  private String tableName;
  private String schemaName;
  private String remarks;
  private DBTableType tableType;

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getRemarks() {
    return this.remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getTableType() {
    return tableType.name();
  }

  public void setTableType(String tableType) {
    this.tableType = DBTableType.valueOf(tableType.toUpperCase());
  }

  public boolean isViewTable() {
    return DBTableType.VIEW == tableType;
  }
}
