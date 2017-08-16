package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

public final class IndexSeekingOuterRewriter<T extends BasketWriter<T>> extends UnmatchedOuterRewriter<T> {

  private final Path path;

  IndexSeekingOuterRewriter(T target, Path path, Updater updater) {
    super(target, updater);

    this.path = path;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return startObject(target, path, this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return startArray(target, path, this, updater);
  }


}
