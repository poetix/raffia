package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.baskets.Basket;

public interface BasketPredicate extends ValuePredicate<Basket> {
  boolean test(Basket basket);
}
