package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;

import java.math.BigDecimal;

final class PredicateMatchingInnerRewriter<T extends BasketWriter<T>> extends InnerRewriter<T> {

  private final Visitor<Basket> itemUpdater;

  PredicateMatchingInnerRewriter(T target, StreamingRewriter<T> parent, Visitor<Basket> itemUpdater) {
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
