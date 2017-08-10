package com.codepoetics.raffia.indexes.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.inner.Inner;

final class PredicateMatchingOuter<T extends BasketWriter<T>> extends UnmatchedOuter<T> {

  PredicateMatchingOuter(T target, Visitor<Basket> updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return Inner.predicateMatching(getTarget().beginObject(), this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return Inner.predicateMatching(getTarget().beginArray(), this, updater);
  }

}
