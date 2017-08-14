package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.baskets.Basket;

public final class Updaters {

  public static final Updater NO_OP = new Updater() {
    @Override
    public Basket update(Basket basket) {
      return basket;
    }
  };

  public static Updater branch(final BasketPredicate predicate, final Updater ifTrue, final Updater ifFalse) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return predicate.test(basket)
            ? ifTrue.update(basket)
            : ifFalse.update(basket);
      }
    };
  }

  public static Updater toConstant(final Basket value) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return value;
      }
    };
  }
}
