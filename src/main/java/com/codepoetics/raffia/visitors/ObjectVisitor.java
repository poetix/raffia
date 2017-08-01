package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ObjectVisitor implements Visitor<Object> {

  @Override
  public Object visitString(String value) {
    return value;
  }

  @Override
  public Object visitBoolean(boolean value) {
    return value;
  }

  @Override
  public Object visitNumber(BigDecimal value) {
    return value;
  }

  @Override
  public Object visitNull() {
    return null;
  }

  @Override
  public Object visitArray(ArrayContents items) {
    List<Object> objectifiedItems = new ArrayList<>();
    for (Basket item : items) {
      objectifiedItems.add(item.visit(this));
    }
    return objectifiedItems;
  }

  @Override
  public Object visitObject(PropertySet properties) {
    Map<String, Object> objectifiedProperties =  new LinkedHashMap<>();
    for (ObjectEntry entry : properties) {
      objectifiedProperties.put(entry.getKey(), entry.getValue().visit(this));
    }
    return objectifiedProperties;
  }
}
