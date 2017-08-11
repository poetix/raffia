package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;

import java.math.BigDecimal;

final class MatchedInnerProjector<T extends BasketWriter<T>> extends InnerProjector<T> {

  MatchedInnerProjector(T target, StreamingProjector<T> parent) {
    super(target, parent);
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new MatchedInnerProjector<>(newTarget, parent);
  }

  private FilteringWriter<T> enter(T newTarget) {
    return new MatchedInnerProjector<>(newTarget, this);
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
  public FilteringWriter<T> end() {
    return parent.advance(getTarget().end());
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
