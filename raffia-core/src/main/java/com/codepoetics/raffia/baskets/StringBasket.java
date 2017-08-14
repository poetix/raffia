package com.codepoetics.raffia.baskets;


final class StringBasket extends ValueBasket<String> {

  StringBasket(String value) {
    super(value);
  }

  @Override
  public BasketType getType() {
    return BasketType.STRING;
  }

  @Override
  public boolean isString() {
    return true;
  }

  @Override
  public String asString() {
    return value;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitString(value);
  }

  @Override
  public String toString() {
    return "\"" + value + "\"";
  }

}
