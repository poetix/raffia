package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Visitor;

final class NullBasket implements Basket {

  static final Basket INSTANCE = new NullBasket();

  private NullBasket() {
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitNull();
  }

  @Override
  public boolean equals(Object other) {
    return this == other || other instanceof NullBasket;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String toString() {
    return "";
  }

}
