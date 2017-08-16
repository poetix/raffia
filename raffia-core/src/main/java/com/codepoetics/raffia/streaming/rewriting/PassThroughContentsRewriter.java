package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

final class PassThroughContentsRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  private int depth = 0;

  PassThroughContentsRewriter(T target, StreamingRewriter<T> parent) {
    super(target, parent, null);
  }

  @Override
  public FilteringWriter<T> end() {
    target = target.end();
    return depth-- == 0 ? parent.advance(target) : this;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    target = target.beginObject();
    depth ++;
    return this;
  }

  @Override
  public FilteringWriter<T> beginArray() {
    target = target.beginArray();
    depth ++;
    return this;
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(target.key(key));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return advance(target.addNull());
  }
}
