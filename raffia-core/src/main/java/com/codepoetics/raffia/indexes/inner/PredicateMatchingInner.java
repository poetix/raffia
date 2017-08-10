package com.codepoetics.raffia.indexes.inner;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;

import java.math.BigDecimal;

final class PredicateMatchingInner<T extends BasketWriter<T>> extends Inner<T> {

  private final Visitor<Basket> itemUpdater;

  PredicateMatchingInner(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> itemUpdater) {
    super(target, parent);
    this.itemUpdater = itemUpdater;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new PredicateMatchingInner<>(newTarget, parent, itemUpdater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return Inner.matchedObject(getTarget(), this, itemUpdater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return Inner.matchedArray(getTarget(), this, itemUpdater);
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(getTarget().key(key));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return updated(itemUpdater.visitString(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return updated(itemUpdater.visitNumber(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return updated(itemUpdater.visitBoolean(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return updated(itemUpdater.visitNull());
  }
}
