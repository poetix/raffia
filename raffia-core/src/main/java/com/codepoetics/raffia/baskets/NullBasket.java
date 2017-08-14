package com.codepoetics.raffia.baskets;

final class NullBasket extends Basket {

  static final Basket INSTANCE = new NullBasket();

  private NullBasket() { }

  @Override
  public BasketType getType() {
    return BasketType.NULL;
  }

  @Override
  public <T> T getValue() {
    return null;
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public Void asNull() {
    return null;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitNull();
  }

  @Override
  public boolean equals(Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String toString() {
    return "<null>";
  }

}
