package com.codepoetics.raffia.baskets;

import java.math.BigDecimal;


final class NumberBasket extends ValueBasket<BigDecimal> {

  NumberBasket(BigDecimal value) {
    super(value);
  }

  @Override
  public BasketType getType() {
    return BasketType.NUMBER;
  }

  @Override
  public boolean isNumber() {
    return true;
  }

  @Override
  public BigDecimal asNumber() {
    return value;
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
