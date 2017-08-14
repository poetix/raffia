package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.operations.ValuePredicate;

public final class StringPredicates {

  private StringPredicates() {
  }

  public static ValuePredicate<String> isEqualTo(final String other) {
    return new ValuePredicate<String>() {
      @Override
      public boolean test(String input) {
        return input.equals(other);
      }
    };
  }

}
