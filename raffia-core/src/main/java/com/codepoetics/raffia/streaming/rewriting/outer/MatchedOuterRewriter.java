package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class MatchedOuterRewriter<T extends BasketWriter<T>> extends OuterRewriter<T> {

  MatchedOuterRewriter(T target, Updater updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerRewriter.matchedObject(target, this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerRewriter.matchedArray(target, this, updater);
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
