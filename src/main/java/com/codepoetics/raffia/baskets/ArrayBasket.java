package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Visitor;
import org.pcollections.PVector;

import java.util.List;


final class ArrayBasket extends BaseBasket<PVector<Basket>> {

  ArrayBasket(PVector<Basket> value) {
    super(value);
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitArray(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
