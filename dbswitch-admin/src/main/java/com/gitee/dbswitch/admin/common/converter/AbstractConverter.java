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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractConverter<U, V> implements Converter<U, V> {

  public List<V> convert(List<U> inputList) {
    if (null == inputList) {
      return null;
    }

    return inputList.stream()
        .map(u -> convert(u))
        .collect(Collectors.toList());
  }

  public Set<V> convert(Set<U> inputSet) {
    if (null == inputSet) {
      return null;
    }

    return inputSet.stream()
        .map(u -> convert(u))
        .collect(Collectors.toSet());
  }

}
