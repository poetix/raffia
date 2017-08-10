package com.codepoetics.raffia.indexes.inner;

import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;

import java.math.BigDecimal;

final class PassThroughContentsUpdater<T extends BasketWriter<T>> extends Inner<T> {

  PassThroughContentsUpdater(T target, MatchSeekingUpdater<T> parent) {
    super(target, parent);
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new PassThroughContentsUpdater<>(newTarget, parent);
  }

  private FilteringWriter<T> enter(T newTarget) {
    return new PassThroughContentsUpdater<>(newTarget, this);
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
