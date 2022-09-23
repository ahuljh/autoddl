/**
 * Copyright 2022 bejson.com
 */
package com.gitee.dbswitch.common.entity;

/**
 * Auto-generated: 2022-09-22 11:27:33
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class QryDDLInput {
    public QryDDLInput() {
    }

    public QryDDLInput(String type, String host, int port, String mode, String user, String passwd, String dbname, String charset, String src_model, String src_table, String target, String dest_model, String dest_table) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.mode = mode;
        this.user = user;
        this.passwd = passwd;
        this.dbname = dbname;
        this.charset = charset;
        this.src_model = src_model;
        this.src_table = src_table;
        this.target = target;
        this.dest_model = dest_model;
        this.dest_table = dest_table;
    }

    private String type;
    private String host;
    private int port;
    private String mode;
    private String user;
    private String passwd;
    private String dbname;
    private String charset;
    private String src_model;
    private String src_table;
    private String target;
    private String dest_model;
    private String dest_table;

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
    public String getMode() {
        return mode;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public String getUser() {
        return user;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
    public String getPasswd() {
        return passwd;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }
    public String getDbname() {
        return dbname;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    public String getCharset() {
        return charset;
    }

    public void setSrc_model(String src_model) {
        this.src_model = src_model;
    }
    public String getSrc_model() {
        return src_model;
    }

    public void setSrc_table(String src_table) {
        this.src_table = src_table;
    }
    public String getSrc_table() {
        return src_table;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    public String getTarget() {
        return target;
    }

    public void setDest_model(String dest_model) {
        this.dest_model = dest_model;
    }
    public String getDest_model() {
        return dest_model;
    }

    public void setDest_table(String dest_table) {
        this.dest_table = dest_table;
    }
    public String getDest_table() {
        return dest_table;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\"type\": \""+type +"\",\n" +
                "\"host\": \""+  host +"\",\n" +
                "\"port\": "+  port +",\n" +
                "\"mode\": \""+  mode +"\",\n" +
                "\"user\": \""+  user +"\",\n" +
                "\"passwd\": \""+  passwd +"\",\n" +
                "\"dbname\": \""+  dbname +"\",\n" +
                "\"charset\": \""+  charset +"\",\n" +
                "\"src_model\": \""+  src_model +"\",\n" +
                "\"src_table\": \""+  src_table +"\",\n" +
                "\"target\": \""+  target +"\",\n" +
                "\"dest_model\": \""+  dest_model +"\",\n" +
                "\"dest_table\": \""+  dest_table +"\",\n" +
                "}";
    }
}