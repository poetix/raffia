package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class PredicateMatchingInnerRewriter<T extends BasketWriter<T>> extends InnerRewriter<T> {

  private final Updater itemUpdater;

  PredicateMatchingInnerRewriter(T target, StreamingRewriter<T> parent, Updater itemUpdater) {
    super(target, parent);
    this.itemUpdater = itemUpdater;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new PredicateMatchingInnerRewriter<>(newTarget, parent, itemUpdater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerRewriter.matchedObject(getTarget(), this, itemUpdater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerRewriter.matchedArray(getTarget(), this, itemUpdater);
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(getTarget().key(key));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return updated(itemUpdater.update(Basket.ofString(value)));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return updated(Basket.ofNumber(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return updated(Basket.ofBoolean(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return updated(Basket.ofNull());
  }
}
