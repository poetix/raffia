package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.api.Mapper;

public final class StringPredicates {

  private StringPredicates() {
  }

  public static Mapper<String, Boolean> isEqualTo(final String other) {
    return new Mapper<String, Boolean>() {
      @Override
      public Boolean map(String input) {
        return input.equals(other);
      }
    };
  }

}
