package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.ArrayContents;
import com.codepoetics.raffia.api.Visitor;


final class ArrayBasket extends BaseBasket<ArrayContents> {

  ArrayBasket(ArrayContents value) {
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
