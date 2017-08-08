package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.ArrayContents;
import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.PropertySet;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

abstract class StructProjector<T> implements Visitor<List<T>> {

  @Override
  public List<T> visitString(String value) {
    return Collections.emptyList();
  }

  @Override
  public List<T> visitBoolean(boolean value) {
    return Collections.emptyList();
  }

  @Override
  public List<T> visitNumber(BigDecimal value) {
    return Collections.emptyList();
  }

  @Override
  public List<T> visitNull() {
    return Collections.emptyList();
  }

}
