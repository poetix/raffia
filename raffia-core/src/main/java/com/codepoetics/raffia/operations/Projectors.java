package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.mappers.Mapper;

import java.math.BigDecimal;

public final class Projectors {

  public static final Projector<String> asString = new Projector<String>() {
    @Override
    public ProjectionResult<String> project(Basket basket) {
      return basket.isString() ? ProjectionResult.ofSingle(basket.asString()) : ProjectionResult.<String>empty();
    }
  };
  public static final Projector<Boolean> asBoolean = new Projector<Boolean>() {
    @Override
    public ProjectionResult<Boolean> project(Basket basket) {
      return basket.isBoolean() ? ProjectionResult.ofSingle(basket.asBoolean()) : ProjectionResult.<Boolean>empty();
    }
  };
  public static final Projector<BigDecimal> asNumber = new Projector<BigDecimal>() {
    @Override
    public ProjectionResult<BigDecimal> project(Basket basket) {
      return basket.isNumber() ? ProjectionResult.ofSingle(basket.asNumber()) : ProjectionResult.<BigDecimal>empty();
    }
  };
  public static final Projector<Void> asNull = new Projector<Void>() {
    @Override
    public ProjectionResult<Void> project(Basket basket) {
      return basket.isNull() ? ProjectionResult.ofSingle(basket.asNull()) : ProjectionResult.<Void>empty();
    }
  };
  public static final Projector<ArrayContents> asArray = new Projector<ArrayContents>() {
    @Override
    public ProjectionResult<ArrayContents> project(Basket basket) {
      return basket.isArray() ? ProjectionResult.ofSingle(basket.asArray()) : ProjectionResult.<ArrayContents>empty();
    }
  };
  public static final Projector<PropertySet> asObject = new Projector<PropertySet>() {
    @Override
    public ProjectionResult<PropertySet> project(Basket basket) {
      return basket.isObject() ? ProjectionResult.ofSingle(basket.asObject()) : ProjectionResult.<PropertySet>empty();
    }
  };

  private Projectors() {
  }

  private static final Projector<Basket> ID = new Projector<Basket>() {
    @Override
    public ProjectionResult<Basket> project(Basket basket) {
      return ProjectionResult.ofSingle(basket);
    }
  };

  private static final Projector<Object> ALWAYS_EMPTY = new Projector<Object>() {
    @Override
    public ProjectionResult<Object> project(Basket basket) {
      return ProjectionResult.empty();
    }
  };

  public static Projector<Basket> id() {
    return ID;
  }

  public static <T> Projector<T> constant(final T value) {
    return new Projector<T>() {
      @Override
      public ProjectionResult<T> project(Basket basket) {
        return ProjectionResult.ofSingle(value);
      }
    };
  }

  public static <T> Projector<T> alwaysEmpty() {
    return (Projector<T>) ALWAYS_EMPTY;
  }

  public static <T> Projector<T> branch(final BasketPredicate predicate, final Projector<T> ifTrue, final Projector<T> ifFalse) {
    return new Projector<T>() {
      @Override
      public ProjectionResult<T> project(Basket basket) {
        return (predicate.test(basket)) ? ifTrue.project(basket) : ifFalse.project(basket);
      }
    };
  }

  public static <T> Projector<T> atIndex(final int index, final Projector<T> itemProjection) {
    return new Projector<T>() {
      @Override
      public ProjectionResult<T> project(Basket basket) {
        if (!basket.isArray()) {
          return ProjectionResult.empty();
        }

        ArrayContents contents = basket.asArray();
        int actual = index < 0 ? contents.size() - index : index;
        if (actual < 0 || actual >= contents.size()) {
          return ProjectionResult.empty();
        }
        return itemProjection.project(contents.get(actual));
      }
    };
  }

  public static <T> Projector<T> atKey(final String key, final Projector<T> itemProjection) {
    return new Projector<T>() {
      @Override
      public ProjectionResult<T> project(Basket basket) {
        if (basket.isObject()) {
          return ProjectionResult.empty();
        }

        PropertySet properties = basket.asObject();
        if (!properties.containsKey(key)) {
          return ProjectionResult.empty();
        }

        return itemProjection.project(properties.get(key));
      }
    };
  }

  public static <O> Projector<O> flatMap(final Projector<Basket> left, final Projector<O> right) {
    return new Projector<O>() {
      @Override
      public ProjectionResult<O> project(Basket basket) {
        ProjectionResult<O> result = ProjectionResult.empty();
        for (Basket item : left.project(basket)) {
          result.add(right.project(item));
        }
        return result;
      }
    };
  }

  public static <I, O> Projector<O> feedback(final Projector<I> left, final Mapper<I, Projector<O>> right) {
    return new Projector<O>() {
      @Override
      public ProjectionResult<O> project(Basket basket) {
        ProjectionResult<O> result = ProjectionResult.empty();

        for (I item : left.project(basket)) {
          result = result.add(right.map(item).project(basket));
        }

        return result;
      }
    };
  }
}
