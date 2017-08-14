package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;

import java.math.BigDecimal;

final class IgnoreAllProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  protected final StreamingProjector<T> parent;

  protected IgnoreAllProjector(T target, StreamingProjector<T> parent) {
    super(target);
    this.parent = parent;
  }

  private FilteringWriter<T> enter() {
    return new IgnoreAllProjector<>(getTarget(), this);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return enter();
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return enter();
  }

  @Override
  public FilteringWriter<T> end() {
    return parent.advance(getTarget());
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> addNull() {
    return ignore();
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return this;
  }
}
