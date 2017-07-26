package com.codepoetics.raffia.api;

public interface Basket {

  <T> T visit(Visitor<T> visitor);

}
