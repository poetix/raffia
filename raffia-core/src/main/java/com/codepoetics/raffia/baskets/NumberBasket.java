package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Visitor;

import java.math.BigDecimal;


final class NumberBasket extends BaseBasket<BigDecimal> {

  NumberBasket(BigDecimal value) {
    super(value);
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitNumber(value);
  }

  @Override
  public String toString() {
    return "<" + value + ">";
  }

}
