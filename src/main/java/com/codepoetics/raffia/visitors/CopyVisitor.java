package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.PropertySet;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;
import org.pcollections.PVector;

import java.math.BigDecimal;

final class CopyVisitor implements Visitor<Basket> {

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

  @Override
  public Basket visitArray(PVector<Basket> items) {
    return Baskets.ofArray(items);
  }

  @Override
  public Basket visitObject(PropertySet properties) {
    return Baskets.ofObject(properties);
  }

}
