package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.api.Mapper;

import java.math.BigDecimal;

public final class NumberPredicates {

  private NumberPredicates() {
  }

  public static Mapper<BigDecimal, Boolean> isEqualTo(int other) {
    return isEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isEqualTo(long other) {
    return isEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isEqualTo(double other) {
    return isEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isEqualTo(String other) {
    return isEqualTo(new BigDecimal(other));
  }

  public static Mapper<BigDecimal, Boolean> isEqualTo(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.equals(other);
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThan(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) > 0;
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isLessThan(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) < 0;
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThanOrEqualTo(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) >= 0;
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isLessThanOrEqualTo(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) <= 0;
      }
    };
  }
}
