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

  public static Mapper<BigDecimal, Boolean> isGreaterThan(int other) {
    return isGreaterThan(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThan(long other) {
    return isGreaterThan(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThan(double other) {
    return isGreaterThan(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThan(String other) {
    return isGreaterThan(new BigDecimal(other));
  }
  
  public static Mapper<BigDecimal, Boolean> isGreaterThan(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) > 0;
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isLessThan(int other) {
    return isLessThan(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isLessThan(long other) {
    return isLessThan(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isLessThan(double other) {
    return isLessThan(BigDecimal.valueOf(other));
  }
  
  public static Mapper<BigDecimal, Boolean> isLessThan(String other) {
    return isLessThan(new BigDecimal(other));
  }
  
  public static Mapper<BigDecimal, Boolean> isLessThan(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) < 0;
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThanOrEqualTo(int other) {
    return isGreaterThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThanOrEqualTo(long other) {
    return isGreaterThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThanOrEqualTo(double other) {
    return isGreaterThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isGreaterThanOrEqualTo(String other) {
    return isGreaterThanOrEqualTo(new BigDecimal(other));
  }
  
  public static Mapper<BigDecimal, Boolean> isGreaterThanOrEqualTo(final BigDecimal other) {
    return new Mapper<BigDecimal, Boolean>() {
      @Override
      public Boolean map(BigDecimal input) {
        return input.compareTo(other) >= 0;
      }
    };
  }

  public static Mapper<BigDecimal, Boolean> isLessThanOrEqualTo(int other) {
    return isLessThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isLessThanOrEqualTo(long other) {
    return isLessThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isLessThanOrEqualTo(double other) {
    return isLessThanOrEqualTo(BigDecimal.valueOf(other));
  }

  public static Mapper<BigDecimal, Boolean> isLessThanOrEqualTo(String other) {
    return isLessThanOrEqualTo(new BigDecimal(other));
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
