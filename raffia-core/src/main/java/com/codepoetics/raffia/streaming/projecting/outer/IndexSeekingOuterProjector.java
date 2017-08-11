package com.codepoetics.raffia.streaming.projecting.outer;

import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;

public final class IndexSeekingOuterProjector<T extends BasketWriter<T>> extends UnmatchedOuterProjector<T> {

  private final Path path;

  IndexSeekingOuterProjector(T target, Path path) {
    super(target);

    this.path = path;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return startObject(getTarget(), path, this);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return startArray(getTarget(), path, this);
  }


}
