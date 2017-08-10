package com.codepoetics.raffia.indexes.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;

import java.math.BigDecimal;

public final class IndexSeekingOuter<T extends BasketWriter<T>> extends UnmatchedOuter<T> {

  private final Path path;

  IndexSeekingOuter(T target, Path path, Visitor<Basket> updater) {
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
