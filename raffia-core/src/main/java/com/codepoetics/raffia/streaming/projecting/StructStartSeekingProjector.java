package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class StructStartSeekingProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  private final Path path;

  public StructStartSeekingProjector(T target, Path path) {
    super(target, null);
    this.path = path;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return StreamingProjector.startObject(target, path, this);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return StreamingProjector.startArray(target, path, this);
  }

  @Override
  public FilteringWriter<T> end() {
    throw new IllegalStateException("end() called when not writing object or array");
  }

  @Override
  public FilteringWriter<T> key(String key) {
    throw new IllegalStateException("key() called when not writing object");
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return new EndOfLineProjector<>(target);
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return new EndOfLineProjector<>(target);
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return new EndOfLineProjector<>(target);
  }

  @Override
  public FilteringWriter<T> addNull() {
    return new EndOfLineProjector<>(target);
  }
}
