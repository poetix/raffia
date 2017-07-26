package com.codepoetics.raffia.api;

public interface KeyValuePredicate {
  boolean test(String key, Basket value);
}
