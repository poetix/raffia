package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class MatchedProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  private int depth = 0;

  MatchedProjector(T target, FilteringWriter<T> parent) {
    super(target, parent);
    this.parent = parent;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    target = target.beginObject();
    depth += 1;
    return this;
  }

  @Override
  public FilteringWriter<T> beginArray() {
    target = target.beginArray();
    depth +=1;
    return this;
  }

  @Override
  public FilteringWriter<T> end() {
    target = target.end();

    if (depth-- > 0) {
      return this;
    }

    if (parent == null) {
      throw new IllegalStateException("end() called when not writing object or array");
    }

    return parent.advance(target);
  }

  @Override
  public FilteringWriter<T> key(String key) {
    target = target.key(key);
    return this;
  }

  @Override
  public FilteringWriter<T> add(String value) {
    target = target.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    target = target.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    target = target.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> addNull() {
    target = target.addNull();
    return this;
  }
}
