package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class PredicateMatchingRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  PredicateMatchingRewriter(T target, FilteringWriter<T> parent, Updater itemUpdater) {
    super(target, parent, itemUpdater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return WeavingRewriter.weavingObject(target, this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return WeavingRewriter.weavingArray(target, this, updater);
  }

  @Override
  public FilteringWriter<T> end() {
    return parent.advance(target.end());
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(target.key(key));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return updated(Basket.ofString(value));
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
