package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Visitor;


final class StringBasket extends BaseBasket<String> {

  StringBasket(String value) {
    super(value);
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
