package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;
import com.codepoetics.raffia.streaming.rewriting.outer.OuterRewriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;

public abstract class StreamingRewriter<T extends BasketWriter<T>> implements FilteringWriter<T> {

  protected T target;

  protected StreamingRewriter(T target) {
    this.target = target;
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> start(T target, Path path, Updater updater) {
    return OuterRewriter.create(target, path, updater);
  }

  private static Updater makeConditionalUpdater(Path path, Updater updater) {
    return path.head().createItemUpdater(path.tail(), updater);
  }

  protected FilteringWriter<T> startArray(T target, Path path, StreamingRewriter<T> parent, Updater updater) {
    if (path.isEmpty()) {
      return InnerRewriter.matchedArray(target, parent, updater);
    }

    if (path.head().isConditional()) {
      return InnerRewriter.predicateMatching(target.beginArray(), parent, makeConditionalUpdater(path, updater));
    }

    return InnerRewriter.arrayIndexSeeking(target.beginArray(), path, parent, updater);
  }

  protected FilteringWriter<T> startObject(T target, Path path, StreamingRewriter<T> parent, Updater updater) {
    if (path.isEmpty()) {
      return InnerRewriter.matchedObject(target, parent, updater);
    }

    if (path.head().isConditional()) {
      return InnerRewriter.predicateMatching(target.beginObject(), parent, makeConditionalUpdater(path, updater));
    }

    return InnerRewriter.objectKeySeeking(target.beginObject(), path, parent, updater);
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
    return advance(basket.visit(Visitors.writingTo(target)));
  }

}
