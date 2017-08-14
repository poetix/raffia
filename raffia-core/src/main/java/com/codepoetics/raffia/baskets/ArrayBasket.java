package com.codepoetics.raffia.baskets;


final class ArrayBasket extends ValueBasket<ArrayContents> {

  ArrayBasket(ArrayContents value) {
    super(value);
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitArray(value);
  }

  @Override
  public BasketType getType() {
    return BasketType.ARRAY;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public ArrayContents asArray() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
