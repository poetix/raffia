package com.codepoetics.raffia.indexes.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;

public abstract class Outer<T extends BasketWriter<T>> extends MatchSeekingUpdater<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> create(T target, Path path, Visitor<Basket> updater) {
    if (path.isEmpty()) {
      return matched(target, updater);
    }

    if (path.head().isConditional()) {
      return predicateMatching(target, path.head().createItemUpdater(path.tail(), updater));
    }

    return indexSeeking(target, path, updater);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> indexSeeking(T target, Path path, Visitor<Basket> updater) {
    return new IndexSeekingOuter<>(target, path, updater);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> predicateMatching(T target, Visitor<Basket> updater) {
    return new PredicateMatchingOuter<>(target, updater);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> matched(T target, Visitor<Basket> updater) {
    return new MatchedOuter<>(target, updater);
  }

  protected final Visitor<Basket> updater;

  protected Outer(T target, Visitor<Basket> updater) {
    super(target);
    this.updater = updater;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new EndOfLine<>(newTarget);
  }

  @Override
  public FilteringWriter<T> key(String key) {
    throw new IllegalStateException("key() called when not writing object");
  }

  @Override
  public FilteringWriter<T> end() {
    throw new IllegalStateException("end() called when not writing struct");
  }

}
