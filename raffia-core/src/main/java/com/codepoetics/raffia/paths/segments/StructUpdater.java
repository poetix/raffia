package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;

import java.math.BigDecimal;

abstract class StructUpdater implements Visitor<Basket> {
  @Override
  public Basket visitString(String value) {
    return Basket.ofString(value);
  }

  @Override
  public Basket visitBoolean(boolean value) {
    return Basket.ofBoolean(value);
  }

  @Override
  public Basket visitNumber(BigDecimal value) {
    return Basket.ofNumber(value);
  }

  @Override
  public Basket visitNull() {
    return Basket.ofNull();
  }
}
