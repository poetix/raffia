package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.operations.ValuePredicate;

import java.math.BigDecimal;

public final class NumberPredicates {

  private NumberPredicates() {
  }

  public static ValuePredicate<BigDecimal> isEqualTo(int other) {
    return isEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isEqualTo(long other) {
    return isEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isEqualTo(double other) {
    return isEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isEqualTo(String other) {
    return isEqualTo(new BigDecimal(other));
  }

  public static ValuePredicate<BigDecimal> isEqualTo(final BigDecimal other) {
    return new ValuePredicate<BigDecimal>() {
      @Override
      public boolean test(BigDecimal input) {
        return input.equals(other);
      }
    };
  }

  public static ValuePredicate<BigDecimal> isGreaterThan(int other) {
    return isGreaterThan(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isGreaterThan(long other) {
    return isGreaterThan(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isGreaterThan(double other) {
    return isGreaterThan(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isGreaterThan(String other) {
    return isGreaterThan(new BigDecimal(other));
  }
  
  public static ValuePredicate<BigDecimal> isGreaterThan(final BigDecimal other) {
    return new ValuePredicate<BigDecimal>() {
      @Override
      public boolean test(BigDecimal input) {
        return input.compareTo(other) > 0;
      }
    };
  }

  public static ValuePredicate<BigDecimal> isLessThan(int other) {
    return isLessThan(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isLessThan(long other) {
    return isLessThan(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isLessThan(double other) {
    return isLessThan(BigDecimal.valueOf(other));
  }
  
  public static ValuePredicate<BigDecimal> isLessThan(String other) {
    return isLessThan(new BigDecimal(other));
  }
  
  public static ValuePredicate<BigDecimal> isLessThan(final BigDecimal other) {
    return new ValuePredicate<BigDecimal>() {
      @Override
      public boolean test(BigDecimal input) {
        return input.compareTo(other) < 0;
      }
    };
  }

  public static ValuePredicate<BigDecimal> isGreaterThanOrEqualTo(int other) {
    return isGreaterThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isGreaterThanOrEqualTo(long other) {
    return isGreaterThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isGreaterThanOrEqualTo(double other) {
    return isGreaterThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isGreaterThanOrEqualTo(String other) {
    return isGreaterThanOrEqualTo(new BigDecimal(other));
  }
  
  public static ValuePredicate<BigDecimal> isGreaterThanOrEqualTo(final BigDecimal other) {
    return new ValuePredicate<BigDecimal>() {
      @Override
      public boolean test(BigDecimal input) {
        return input.compareTo(other) >= 0;
      }
    };
  }

  public static ValuePredicate<BigDecimal> isLessThanOrEqualTo(int other) {
    return isLessThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isLessThanOrEqualTo(long other) {
    return isLessThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isLessThanOrEqualTo(double other) {
    return isLessThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static ValuePredicate<BigDecimal> isLessThanOrEqualTo(String other) {
    return isLessThanOrEqualTo(new BigDecimal(other));
  }
  
  public static ValuePredicate<BigDecimal> isLessThanOrEqualTo(final BigDecimal other) {
    return new ValuePredicate<BigDecimal>() {
      @Override
      public boolean test(BigDecimal input) {
        return input.compareTo(other) <= 0;
      }
    };
  }
}
