// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON序列化与反序列化工具类
 */
@Slf4j
public final class JsonUtils {

  private static ObjectMapper jacksonMapper = new ObjectMapper();

  public static String toJsonString(Object object) {
    if (Objects.nonNull(object)) {
      try {
        return jacksonMapper.writeValueAsString(object);
      } catch (JsonProcessingException e) {
        log.error(" convert object to json string error：{}", object.toString(), e);
      }
    }

    return null;
  }

  public static <T> T toBeanObject(String jsonString, Class<T> clazz) {
    try {
      return jacksonMapper.readValue(jsonString, clazz);
    } catch (JsonProcessingException e) {
      String className = clazz.getSimpleName();
      log.error(" parse json [{}] to class [{}] error：{}", jsonString, className, e);
    }
    return null;
  }

  public static <T> List<T> toBeanList(String jsonString, Class<T> clazz) {
    try {
      return jacksonMapper.readValue(jsonString, getCollectionType(List.class, clazz));
    } catch (JsonProcessingException e) {
      String className = clazz.getSimpleName();
      log.error(" parse json [{}] to class [{}] error：{}", jsonString, className, e);
    }
    return Collections.emptyList();
  }

  private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
    return jacksonMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
  }

}
