package com.codepoetics.raffia.streaming.projecting.outer;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;
import com.codepoetics.raffia.writers.BasketWriter;

public abstract class OuterProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> create(T target, Path path) {
    if (path.isEmpty()) {
      return matched(target);
    }

    if (path.head().isConditional()) {
      return predicateMatching(target, path.head().createItemProjector(path.tail()));
    }

    return indexSeeking(target, path);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> indexSeeking(T target, Path path) {
    return new IndexSeekingOuterProjector<>(target, path);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> predicateMatching(T target, Projector<Basket> itemProjector) {
    return new PredicateMatchingOuterProjector<>(target, itemProjector);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> matched(T target) {
    return new MatchedOuterProjector<>(target);
  }

  protected OuterProjector(T target) {
    super(target);
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new EndOfLineProjector<>(newTarget);
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
