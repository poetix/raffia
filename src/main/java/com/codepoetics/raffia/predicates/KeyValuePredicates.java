package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.KeyPredicate;
import com.codepoetics.raffia.api.KeyValuePredicate;
import com.codepoetics.raffia.api.Visitor;

public final class KeyValuePredicates {

  private KeyValuePredicates() {
  }

  public static final KeyValuePredicate wildcard = new KeyValuePredicate() {
    @Override
    public boolean test(String key, Basket value) {
      return true;
    }
  };

  public static KeyValuePredicate keyEquals(final String expected) {
    return new KeyValuePredicate() {
      @Override
      public boolean test(String key, Basket value) {
        return key.equals(expected);
      }
    };
  }

  public static KeyValuePredicate keyMatches(final KeyPredicate keyPredicate) {
    return new KeyValuePredicate() {
      @Override
      public boolean test(String key, Basket value) {
        return keyPredicate.test(key);
      }
    };
  }

  public static KeyValuePredicate valueMatches(final Visitor<Boolean> valuePredicate) {
    return new KeyValuePredicate() {
      @Override
      public boolean test(String key, Basket value) {
        return value.visit(valuePredicate);
      }
    };
  }

  public static KeyValuePredicate and(final KeyValuePredicate left, final KeyValuePredicate right) {
    return new KeyValuePredicate() {
      @Override
      public boolean test(String key, Basket value) {
        return left.test(key, value) && right.test(key, value);
      }
    };
  }

  public static KeyValuePredicate or(final KeyValuePredicate left, final KeyValuePredicate right) {
    return new KeyValuePredicate() {
      @Override
      public boolean test(String key, Basket value) {
        return left.test(key, value) || right.test(key, value);
      }
    };
  }
}
