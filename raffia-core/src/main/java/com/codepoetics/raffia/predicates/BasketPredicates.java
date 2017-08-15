package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.BasketPredicate;
import com.codepoetics.raffia.operations.ValuePredicate;

import java.math.BigDecimal;

public final class BasketPredicates {

  private BasketPredicates() {
  }

  public static final BasketPredicate isString = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isString();
    }
  };

  public static final BasketPredicate isNumber = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isNumber();
    }
  };

  public static final BasketPredicate isBoolean = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isBoolean();
    }
  };

  public static final BasketPredicate isNull = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isNull();
    }
  };

  public static final BasketPredicate isArray = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isArray();
    }
  };

  public static final BasketPredicate isObject = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isObject();
    }
  };

  public static final BasketPredicate isEmpty = new BasketPredicate() {
    @Override
    public boolean test(Basket basket) {
      return basket.isEmpty();
    }
  };

  public static BasketPredicate hasKey(final String key) {
    return isObject(new ValuePredicate<PropertySet>() {
      @Override
      public boolean test(PropertySet value) {
        return value.containsKey(key);
      }
    });
  }

  public static BasketPredicate hasKey(final String key, final BasketPredicate valuePredicate) {
    return isObject(new ValuePredicate<PropertySet>() {
      @Override
      public boolean test(PropertySet value) {
        return value.containsKey(key) && valuePredicate.test(value.get(key));
      }
    });
  }

  public static BasketPredicate isString(final String expected) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isString() && basket.asString().equals(expected);
      }
    };
  }

  public static BasketPredicate isNumber(final BigDecimal expected) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isNumber() && basket.asNumber().equals(expected);
      }
    };
  }

  public static BasketPredicate isString(final ValuePredicate<String> matcher) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isString() && matcher.test(basket.asString());
      }
    };
  }

  public static BasketPredicate isNumber(final ValuePredicate<BigDecimal> matcher) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isNumber() && matcher.test(basket.asNumber());
      }
    };
  }

  public static BasketPredicate isBoolean(final boolean expected) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isBoolean() && basket.asBoolean() == expected;
      }
    };
  }

  public static final BasketPredicate isTrue = isBoolean(true);
  public static final BasketPredicate isFalse = isBoolean(false);

  public static BasketPredicate isBoolean(final ValuePredicate<Boolean> matcher) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isBoolean() && matcher.test(basket.asBoolean());
      }
    };
  }

  public static BasketPredicate isObject(final ValuePredicate<PropertySet> matcher) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isObject() && matcher.test(basket.asObject());
      }
    };
  }

  public static BasketPredicate isArray(final ValuePredicate<ArrayContents> matcher) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isArray() && matcher.test(basket.asArray());
      }
    };
  }
}
