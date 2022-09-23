// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitee.dbswitch.admin.config.SwaggerConfig;
import com.gitee.dbswitch.common.entity.QryDDLInput;
import com.gitee.dbswitch.common.type.ProductTypeEnum;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.DatabaseDescription;
import com.gitee.dbswitch.core.service.IMetaDataByDescriptionService;
import com.gitee.dbswitch.core.service.impl.MetaDataByDescriptionServiceImpl;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"表结构抽取转换接口"})
@RestController
@RequestMapping(value = SwaggerConfig.API_V1 + "/database")
public class StructureController {
    @RequestMapping(value = "/multi_table_sql_by_union_prefix", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "生成多张表统一前缀的DDL语句", notes = "查询指定表结构转换的建表SQL语句，请求的示例包体格式为：\n"
            + "{\n" +
            "\"type\":\"oracle\",\n" +
            "\"host\":\"172.16.5.162\",\n" +
            "\"port\":1521,\n" +
            "\"mode\":\"sid\",\n" +
            "\"user\":\"system\",\n" +
            "\"passwd\":\"system\",\n" +
            "\"dbname\":\"helowin\",\n" +
            "\"charset\":\"utf-8\",\n" +
            "\"src_model\":\"TEST\",\n" +
            "\"multi_src_table\":\"student,ABANK\",\n" +
            "\"target\":\"oracle\",\n" +
            "\"dest_model\":\"ljh_test\",\n" +
            "\"dest_table_prefix\":\"ods_\"\n" +
            "}")
    public String queryDatabaseMultiTableSQL(@RequestBody String body) {
        JSONObject object = JSON.parseObject(body);
        String type = object.getString("type");
        String host = object.getString("host");
        Integer port = object.getInteger("port");
        String user = object.getString("user");
        String mode = object.getString("mode");
        String passwd = object.getString("passwd");
        String dbname = object.getString("dbname");
        String charset = object.getString("charset");
        String src_model = object.getString("src_model");
        String multi_src_table = object.getString("multi_src_table");
        String target = object.getString("target");
        String dest_model = object.getString("dest_model");
        String dest_table_prefix = object.getString("dest_table_prefix");
        String[] multi_src_table_arr = multi_src_table.split(",");
        StringBuilder sb = new StringBuilder();
        Arrays.stream(multi_src_table_arr).forEach(x -> {
          sb.append(queryDatabaseTableSQL(new QryDDLInput(type,host,port,mode,user,passwd,dbname,charset,src_model,x,target,dest_model,dest_table_prefix+x).toString())+";\n");
        });
        return new String(sb);
    }

    @RequestMapping(value = "/table_sql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "生成单张表的DDL语句", notes = "查询指定表结构转换的建表SQL语句，请求的示例包体格式为：\n"
            + "		{\r\n" +
            "		    \"type\":\"oracle\",  \r\n" +
            "		    \"host\":\"172.17.20.52\",\r\n" +
            "		    \"port\":1521,\r\n" +
            "       \"mode\":\"sid\",\r\n" +
            "		    \"user\":\"yi_bo\",\r\n" +
            "		    \"passwd\":\"tangyibo\",\r\n" +
            "		    \"dbname\":\"orcl\",\r\n" +
            "		    \"charset\":\"utf-8\",\r\n" +
            "		    \"src_model\":\"YI_BO\",\r\n" +
            "		    \"src_table\":\"C_SEX\",\r\n" +
            "		    \"target\":\"mysql\",  \r\n" +
            "		    \"dest_model\":\"TANG\",\r\n" +
            "		    \"dest_table\":\"my_test_table\"\r\n" +
            "		}")
    public String queryDatabaseTableSQL(@RequestBody String body) {
        JSONObject object = JSON.parseObject(body);
        String type = object.getString("type");
        String host = object.getString("host");
        Integer port = object.getInteger("port");
        String user = object.getString("user");
        String mode = object.getString("mode");
        String passwd = object.getString("passwd");
        String dbname = object.getString("dbname");
        String charset = object.getString("charset");
        String src_model = object.getString("src_model");
        String src_table = object.getString("src_table");
        String target = object.getString("target");
        String dest_model = object.getString("dest_model");
        String dest_table = object.getString("dest_table");

        if (null != type && null != mode && type.equalsIgnoreCase("oracle")
                && mode.equalsIgnoreCase("TNSNAME")) {
            if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(passwd)
                    || Strings.isNullOrEmpty(dbname)) {
                throw new RuntimeException("Invalid input parameter");
            }

            if (Strings.isNullOrEmpty(charset) || Strings.isNullOrEmpty(src_model)
                    || Strings.isNullOrEmpty(src_table) || Strings.isNullOrEmpty(dest_model)
                    || Strings.isNullOrEmpty(dest_table) || Strings.isNullOrEmpty(target)) {
                throw new RuntimeException("Invalid input parameter");
            }

            if (Objects.isNull(port)) {
                port = 0;
            }

        } else {
            if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host)
                    || Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(passwd)
                    || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
                    || Strings.isNullOrEmpty(src_model) || Strings.isNullOrEmpty(src_table)
                    || Objects.isNull(port) || Strings.isNullOrEmpty(dest_model)
                    || Strings.isNullOrEmpty(dest_table) || Strings.isNullOrEmpty(target)) {
                throw new RuntimeException("Invalid input parameter");
            }
        }

        DatabaseDescription databaseDesc = new DatabaseDescription(type,
                host, port, mode, dbname, charset, user, passwd);
        IMetaDataByDescriptionService migrationService =
                new MetaDataByDescriptionServiceImpl(databaseDesc);

        List<ColumnDescription> columnDescs = migrationService
                .queryTableColumnMeta(src_model, src_table);
        List<String> primaryKeys = migrationService.queryTablePrimaryKeys(src_model, src_table);
        ProductTypeEnum targetDatabaseType = ProductTypeEnum.valueOf(target.trim().toUpperCase());
        String sql = migrationService
                .getDDLCreateTableSQL(targetDatabaseType, columnDescs, primaryKeys, dest_model, dest_table,
                        false);
        return sql;
    }
}