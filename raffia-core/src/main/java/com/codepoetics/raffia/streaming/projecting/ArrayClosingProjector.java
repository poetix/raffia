package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class ArrayClosingProjector<T extends BasketWriter<T>> implements FilteringWriter<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> closing(FilteringWriter<T> inner) {
    return new ArrayClosingProjector<>(inner);
  }

  private FilteringWriter<T> inner;

  private ArrayClosingProjector(FilteringWriter<T> inner) {
    this.inner = inner;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    inner = inner.beginObject();
    return this;
  }

  @Override
  public FilteringWriter<T> beginArray() {
    inner = inner.beginArray();
    return this;
  }

  @Override
  public FilteringWriter<T> end() {
    inner = inner.end();
    return this;
  }

  @Override
  public FilteringWriter<T> key(String key) {
    inner = inner.key(key);
    return this;
  }

  @Override
  public FilteringWriter<T> add(String value) {
    inner = inner.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    inner = inner.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    inner = inner.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> addNull() {
    inner = inner.addNull();
    return this;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    inner = inner.advance(newTarget);
    return this;
  }

  @Override
  public T complete() {
    return inner.complete().end();
  }
}
