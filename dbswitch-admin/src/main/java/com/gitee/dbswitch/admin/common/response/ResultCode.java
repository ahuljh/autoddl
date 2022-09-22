// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

  SUCCESS(0, "操作成功"),
  ERROR_INTERNAL_ERROR(1, "内部错误"),
  ERROR_INVALID_ARGUMENT(2, "无效参数"),
  ERROR_RESOURCE_NOT_EXISTS(3, "资源不存在"),
  ERROR_RESOURCE_ALREADY_EXISTS(4, "资源已存在"),
  ERROR_RESOURCE_NOT_DEPLOY(5, "资源未发布"),
  ERROR_RESOURCE_HAS_DEPLOY(6, "资源已发布"),
  ERROR_USER_NOT_EXISTS(7, "用户不存在"),
  ERROR_USER_PASSWORD_WRONG(8, "密码错误"),
  ERROR_INVALID_JDBC_URL(9, "JDBC连接的URL格式不正确"),
  ERROR_CANNOT_CONNECT_REMOTE(10, "远程地址不可达"),
  ERROR_INVALID_ASSIGNMENT_CONFIG(11, "无效的任务参数配置"),

  ERROR_ACCESS_FORBIDDEN(403, "无效的登陆凭证"),
  ERROR_TOKEN_EXPIRED(404, "登录凭证已失效"),
  ;

  private int code;
  private String message;
}
