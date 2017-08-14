package com.codepoetics.raffia.baskets;

final class ObjectBasket extends ValueBasket<PropertySet> {

  ObjectBasket(PropertySet value) {
    super(value);
  }

  @Override
  public BasketType getType() {
    return BasketType.OBJECT;
  }

  @Override
  public boolean isObject() {
    return true;
  }

  @Override
  public PropertySet asObject() {
    return value;
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
