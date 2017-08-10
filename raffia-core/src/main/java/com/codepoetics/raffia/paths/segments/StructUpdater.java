package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;

import java.math.BigDecimal;

abstract class StructUpdater implements Visitor<Basket> {
  @Override
  public Basket visitString(String value) {
    return Baskets.ofString(value);
  }

  @Override
  public Basket visitBoolean(boolean value) {
    return Baskets.ofBoolean(value);
  }

  @Override
  public Basket visitNumber(BigDecimal value) {
    return Baskets.ofNumber(value);
  }

  @Override
  public Basket visitNull() {
    return Baskets.ofNull();
  }
}
