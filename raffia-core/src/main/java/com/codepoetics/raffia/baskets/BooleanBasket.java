package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Visitor;


final class BooleanBasket extends BaseBasket<Boolean> {

  static final Basket TRUE = new BooleanBasket(true);
  static final Basket FALSE = new BooleanBasket(false);

  private BooleanBasket(Boolean value) {
    super(value);
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitBoolean(value);
  }

  @Override
  public String toString() {
    return "<" + value + ">";
  }

}
