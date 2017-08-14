package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;

public abstract class OuterRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> create(T target, Path path, Updater updater) {
    if (path.isEmpty()) {
      return matched(target, updater);
    }

    if (path.head().isConditional()) {
      return predicateMatching(target, path.head().createItemUpdater(path.tail(), updater));
    }

    return indexSeeking(target, path, updater);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> indexSeeking(T target, Path path, Updater updater) {
    return new IndexSeekingOuterRewriter<>(target, path, updater);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> predicateMatching(T target, Updater updater) {
    return new PredicateMatchingOuterRewriter<>(target, updater);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> matched(T target, Updater updater) {
    return new MatchedOuterRewriter<>(target, updater);
  }

  protected final Updater updater;

  protected OuterRewriter(T target, Updater updater) {
    super(target);
    this.updater = updater;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new EndOfLineRewriter<>(newTarget);
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
