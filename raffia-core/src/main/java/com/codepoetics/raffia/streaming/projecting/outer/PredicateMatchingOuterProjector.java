package com.codepoetics.raffia.streaming.projecting.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.inner.InnerProjector;

import java.util.List;

final class PredicateMatchingOuterProjector<T extends BasketWriter<T>> extends UnmatchedOuterProjector<T> {

  private final Visitor<List<Basket>> projector;

  PredicateMatchingOuterProjector(T target, Visitor<List<Basket>> projector) {
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