package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;
import com.codepoetics.raffia.streaming.rewriting.outer.OuterRewriter;
import com.codepoetics.raffia.visitors.Visitors;

public abstract class StreamingRewriter<T extends BasketWriter<T>> extends FilteringWriter<T> {

  protected StreamingRewriter(T target) {
    super(target);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> start(T target, Path path, Visitor<Basket> updater) {
    return OuterRewriter.create(target, path, updater);
  }

  private static Visitor<Basket> makeConditionalUpdater(Path path, Visitor<Basket> updater) {
    return path.head().createItemUpdater(path.tail(), updater);
  }

  protected FilteringWriter<T> startArray(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    if (path.isEmpty()) {
      return InnerRewriter.matchedArray(target, parent, updater);
    }

    if (path.head().isConditional()) {
      return InnerRewriter.predicateMatching(target.beginArray(), parent, makeConditionalUpdater(path, updater));
    }

    return InnerRewriter.arrayIndexSeeking(target.beginArray(), path, parent, updater);
  }

  protected FilteringWriter<T> startObject(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    if (path.isEmpty()) {
      return InnerRewriter.matchedObject(target, parent, updater);
    }

    if (path.head().isConditional()) {
      return InnerRewriter.predicateMatching(target.beginObject(), parent, makeConditionalUpdater(path, updater));
    }

    return InnerRewriter.objectKeySeeking(target.beginObject(), path, parent, updater);
  }

  @Override
  public T complete() {
    return getTarget();
  }

  public abstract FilteringWriter<T> advance(T newTarget);

  protected FilteringWriter<T> updated(Basket basket) {
    return advance(basket.visit(Visitors.writingTo(getTarget())));
  }

}