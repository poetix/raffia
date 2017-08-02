package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.PropertySet;
import com.codepoetics.raffia.api.Visitor;

final class ObjectBasket extends BaseBasket<PropertySet> {

  ObjectBasket(PropertySet value) {
    super(value);
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitObject(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
