package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class IgnoreAllFilter<T extends BasketWriter<T>> implements FilteringWriter<T> {

  private T target;
  private final FilteringWriter<T> parent;
  private int depth = 0;

  public IgnoreAllFilter(T target, FilteringWriter<T> parent) {
    this.target = target;
    this.parent = parent;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    depth++;
    return this;
  }

  @Override
  public FilteringWriter<T> beginArray() {
    depth++;
    return this;
  }

  @Override
  public FilteringWriter<T> end() {
    return depth-- == 0 ? parent.advance(target) : this;
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return this;
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return this;
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return this;
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return this;
  }

  @Override
  public FilteringWriter<T> addNull() {
    return this;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    target = newTarget;
    return this;
  }

  @Override
  public T complete() {
    throw new IllegalStateException("Cannot complete while writing array or object");
  }
}
