package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class EndOfLineFilter<T extends BasketWriter<T>> implements FilteringWriter<T> {

  private final T target;

  public EndOfLineFilter(T target) {
    this.target = target;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    throw new IllegalStateException("beginObject() called after basket complete");
  }

  @Override
  public FilteringWriter<T> beginArray() {
    throw new IllegalStateException("beginArray() called after basket complete");
  }

  @Override
  public FilteringWriter<T> end() {
    throw new IllegalStateException("end() called after basket complete");
  }

  @Override
  public FilteringWriter<T> key(String key) {
    throw new IllegalStateException("key() called after basket complete");
  }

  @Override
  public FilteringWriter<T> add(String value) {
    throw new IllegalStateException("add() called after basket complete");
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    throw new IllegalStateException("add() called after basket complete");
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    throw new IllegalStateException("add() called after basket complete");
  }

  @Override
  public FilteringWriter<T> addNull() {
    throw new IllegalStateException("addNull() called after basket complete");
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    throw new IllegalStateException("advance() called after basket complete");
  }

  @Override
  public T complete() {
    return target;
  }
}
