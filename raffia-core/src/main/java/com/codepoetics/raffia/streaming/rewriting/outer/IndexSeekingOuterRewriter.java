package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;

public final class IndexSeekingOuterRewriter<T extends BasketWriter<T>> extends UnmatchedOuterRewriter<T> {

  private final Path path;

  IndexSeekingOuterRewriter(T target, Path path, Visitor<Basket> updater) {
    super(target, updater);

    this.path = path;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return startObject(getTarget(), path, this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return startArray(getTarget(), path, this, updater);
  }


}
