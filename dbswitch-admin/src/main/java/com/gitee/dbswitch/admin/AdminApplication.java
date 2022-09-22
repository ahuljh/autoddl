// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//@tk.mybatis.spring.annotation.MapperScan("com.gitee.dbswitch.admin.mapper")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AdminApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(AdminApplication.class);
    springApplication.setBannerMode(Banner.Mode.OFF);
    springApplication.run(args);
  }
}
