package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.EndOfLineFilter;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class StructStartSeekingRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  private final Path path;
  private final Updater updater;

  public StructStartSeekingRewriter(T target, Path path, Updater updater) {
    super(target, null, updater);
    this.path = path;
    this.updater = updater;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return StreamingRewriter.startObject(target, path, this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return StreamingRewriter.startArray(target, path, this, updater);
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
  public FilteringWriter<T> advance(T newTarget) {
    return new EndOfLineFilter<>(newTarget);
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
