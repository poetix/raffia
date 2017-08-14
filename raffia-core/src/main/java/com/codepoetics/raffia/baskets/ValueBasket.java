package com.codepoetics.raffia.baskets;

abstract class ValueBasket<T> extends Basket {

  protected final T value;

  protected ValueBasket(T value) {
    this.value = value;
  }

  @Override
  public <V> V getValue() {
    return (V) value;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof ValueBasket
        & ValueBasket.class.cast(other).value.equals(value));
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
