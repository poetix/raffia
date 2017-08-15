package com.codepoetics.raffia.streaming.projecting.outer;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.inner.InnerProjector;
import com.codepoetics.raffia.writers.BasketWriter;

final class PredicateMatchingOuterProjector<T extends BasketWriter<T>> extends UnmatchedOuterProjector<T> {

  private final Projector<Basket> projector;

  PredicateMatchingOuterProjector(T target, Projector<Basket> projector) {
    super(target);
    this.projector = projector;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerProjector.predicateMatching(getTarget(), this, projector);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerProjector.predicateMatching(getTarget(), this, projector);
  }

}
