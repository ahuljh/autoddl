// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.common.converter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConverterFactory {

  private static Map<String, Converter> cache = new ConcurrentHashMap<>();

  public static <T extends Converter> T getConverter(Class<T> clazz) {
    String clazzName = clazz.getName();
    Converter converter = cache.get(clazzName);
    if (Objects.isNull(converter)) {
      try {
        converter = clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        log.error("Error:", e);
      }
    }

    return (T) converter;
  }

  private ConverterFactory() {
  }

}
