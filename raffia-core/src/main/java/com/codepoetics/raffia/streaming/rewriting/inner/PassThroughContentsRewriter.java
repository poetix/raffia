package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class PassThroughContentsRewriter<T extends BasketWriter<T>> extends InnerRewriter<T> {

  PassThroughContentsRewriter(T target, StreamingRewriter<T> parent) {
    super(target, parent);
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new PassThroughContentsRewriter<>(newTarget, parent);
  }

  private FilteringWriter<T> enter(T newTarget) {
    return new PassThroughContentsRewriter<>(newTarget, this);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return enter(getTarget().beginObject());
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return enter(getTarget().beginArray());
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(getTarget().key(key));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return advance(getTarget().addNull());
  }
}
