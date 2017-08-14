package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.baskets.Visitor;

import java.math.BigDecimal;

final class CopyVisitor implements Visitor<Basket> {

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

  @Override
  public Basket visitArray(ArrayContents items) {
    return Basket.ofArray(items);
  }

  @Override
  public Basket visitObject(PropertySet properties) {
    return Basket.ofObject(properties);
  }

}
