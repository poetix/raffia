package com.codepoetics.raffia.api;

public interface IndexValuePredicate {
  boolean test(int index, Basket value);
}
