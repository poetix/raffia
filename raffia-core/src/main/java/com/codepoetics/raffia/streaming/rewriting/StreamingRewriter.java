package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;

public abstract class StreamingRewriter<T extends BasketWriter<T>> implements FilteringWriter<T> {

  protected T target;
  protected final FilteringWriter<T> parent;
  protected final Updater updater;

  protected StreamingRewriter(T target, FilteringWriter<T> parent, Updater updater) {
    this.target = target;
    this.parent = parent;
    this.updater = updater;
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> start(T target, Path path, Updater updater) {
    if (path.isEmpty()) {
      return new MatchedRewriter<>(target, null, updater);
    }

    return new StructStartSeekingRewriter<>(target, path, updater);
  }

  private static Updater makeConditionalUpdater(Path path, Updater updater) {
    return path.head().createItemUpdater(path.tail(), updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> startArray(T target, Path path, FilteringWriter<T> parent, Updater updater) {
    if (path.isEmpty()) {
      return new MatchedRewriter<>(target.beginArray(), parent, updater);
    }

    if (path.head().isConditional()) {
      return new PredicateMatchingRewriter<>(target.beginArray(), parent, makeConditionalUpdater(path, updater));
    }

    return IndexSeekingRewriter.seekingArrayIndex(target.beginArray(), path, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> startObject(T target, Path path, FilteringWriter<T> parent, Updater updater) {
    if (path.isEmpty()) {
      return new MatchedRewriter<>(target.beginObject(), parent, updater);
    }

    if (path.head().isConditional()) {
      return new PredicateMatchingRewriter<>(target.beginObject(), parent, makeConditionalUpdater(path, updater));
    }

    return IndexSeekingRewriter.seekingObjectKey(target.beginObject(), path, parent, updater);
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    target = newTarget;
    return this;
  }

  @Override
  public T complete() {
    return target;
  }

  protected FilteringWriter<T> updated(Basket basket) {
    return advance(updater.update(basket).visit(Visitors.writingTo(target)));
  }

}
