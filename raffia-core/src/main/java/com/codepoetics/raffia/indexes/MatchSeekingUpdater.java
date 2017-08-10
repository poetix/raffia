package com.codepoetics.raffia.indexes;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.indexes.inner.Inner;
import com.codepoetics.raffia.indexes.outer.Outer;
import com.codepoetics.raffia.visitors.Visitors;

public abstract class MatchSeekingUpdater<T extends BasketWriter<T>> extends FilteringWriter<T> {

  public MatchSeekingUpdater(T target) {
    super(target);
  }

  static <T extends BasketWriter<T>> FilteringWriter<T> start(T target, Path path, Visitor<Basket> updater) {
    return Outer.create(target, path, updater);
  }

  private static Visitor<Basket> makeConditionalUpdater(Path path, Visitor<Basket> updater) {
    return path.head().createItemUpdater(path.tail(), updater);
  }

  protected FilteringWriter<T> startArray(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    System.out.println("Array: " + path);
    if (path.isEmpty()) {
      return Inner.matchedArray(target, parent, updater);
    }

    if (path.head().isConditional()) {
      return Inner.predicateMatching(target.beginArray(), parent, makeConditionalUpdater(path, updater));
    }

    return Inner.arrayIndexSeeking(target.beginArray(), path, parent, updater);
  }

  protected FilteringWriter<T> startObject(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    System.out.println("Object: " + path);
    if (path.isEmpty()) {
      return Inner.matchedObject(target, parent, updater);
    }

    if (path.head().isConditional()) {
      return Inner.predicateMatching(target.beginObject(), parent, makeConditionalUpdater(path, updater));
    }

    return Inner.objectKeySeeking(target.beginObject(), path, parent, updater);
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
