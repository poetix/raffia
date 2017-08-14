package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.baskets.Basket;

public interface Projector<T> {
  ProjectionResult<T> project(Basket basket);
}
