package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.baskets.Basket;

public final class Projectors {

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
}
