package com.codepoetics.raffia.baskets;


final class BooleanBasket extends ValueBasket<Boolean> {

  static final Basket TRUE = new BooleanBasket(true);
  static final Basket FALSE = new BooleanBasket(false);

  private BooleanBasket(Boolean value) {
    super(value);
  }
  @Override
  public BasketType getType() {
    return BasketType.BOOLEAN;
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  @Override
  public boolean asBoolean() {
    return value;
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
