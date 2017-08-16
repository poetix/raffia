package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class MatchedRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  MatchedRewriter(T target, FilteringWriter<T> parent, Updater updater) {
    super(target, parent, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return WeavingRewriter.weavingObject(target.beginObject(), this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return WeavingRewriter.weavingObject(target.beginArray(), this, updater);
  }

  @Override
  public FilteringWriter<T> end() {
    if (parent == null) {
      throw new IllegalStateException("end() called when not writing object or array");
    }

    return parent.advance(target.end());
  }

  @Override
  public FilteringWriter<T> key(String key) {
    target = target.key(key);
    return this;
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return updated(updater.update(Basket.ofString(value)));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return updated(updater.update(Basket.ofNumber(value)));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return updated(updater.update(Basket.ofBoolean(value)));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return updated(updater.update(Basket.ofNull()));
  }

}
