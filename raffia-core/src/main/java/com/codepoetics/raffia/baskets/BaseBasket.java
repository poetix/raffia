package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Basket;

abstract class BaseBasket<T> implements Basket {

  protected final T value;

  protected BaseBasket(T value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof BaseBasket
        & BaseBasket.class.cast(other).value.equals(value));
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
