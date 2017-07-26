package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.api.*;

public final class IndexValuePredicates {

  private IndexValuePredicates() {
  }

  public static final IndexValuePredicate wildcard = new IndexValuePredicate() {
    @Override
    public boolean test(int index, Basket value) {
      return true;
    }
  };

  public static IndexValuePredicate indexEquals(final int expected) {
    return new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return index == expected;
      }
    };
  }

  public static IndexValuePredicate indexMatches(final IndexPredicate indexPredicate) {
    return new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return indexPredicate.test(index);
      }
    };
  }

  public static IndexValuePredicate valueMatches(final Visitor<Boolean> valuePredicate) {
    return new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return value.visit(valuePredicate);
      }
    };
  }

  public static IndexValuePredicate and(final IndexValuePredicate left, final IndexValuePredicate right) {
    return new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return left.test(index, value) && right.test(index, value);
      }
    };
  }

  public static IndexValuePredicate or(final IndexValuePredicate left, final IndexValuePredicate right) {
    return new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return left.test(index, value) || right.test(index, value);
      }
    };
  }
}
