// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.config;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  public static final String API_PREFIX = "/dbswitch/admin/api";
  public static final String API_V1 = API_PREFIX + "/v1";
  private static final String API_DEFAULT_PACKAGE = "com.gitee.dbswitch.admin.controller.privateapi";
  private static final String API_COMMON_PACKAGE = "com.gitee.dbswitch.admin.controller.publicapi";

  private ApiInfo createApiInfo() {
    return new ApiInfoBuilder()
        .title("服务API文档")
        .description("在线API文档")
        .version("1.0")
        .build();
  }

  @Bean(value = "privateApi")
  public Docket defaultApi() {
    RequestParameterBuilder ticketPar = new RequestParameterBuilder();
    List<RequestParameter> pars = new ArrayList<>();
    ticketPar.name("token")
        .description("认证所用的Token")
        .in(ParameterType.QUERY)
        .required(false)
        .build();
    pars.add(ticketPar.build());

    return new Docket(DocumentationType.SWAGGER_2)
        .enable(true)
        .groupName("需要认证的接口")
        .apiInfo(createApiInfo())
        .select()
        .apis(RequestHandlerSelectors.basePackage(API_DEFAULT_PACKAGE))
        .paths(PathSelectors.any())
        .build()
        .globalRequestParameters(pars)
        .ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class);
  }

  @Bean(value = "publicApi")
  public Docket publicApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .enable(true)
        .groupName("无需认证的接口")
        .apiInfo(createApiInfo())
        .select()
        .apis(RequestHandlerSelectors.basePackage(API_COMMON_PACKAGE))
        .paths(PathSelectors.any())
        .build()
        .ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class);
  }
}