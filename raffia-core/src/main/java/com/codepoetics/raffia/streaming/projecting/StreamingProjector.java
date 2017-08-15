package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.inner.InnerProjector;
import com.codepoetics.raffia.streaming.projecting.outer.OuterProjector;
import com.codepoetics.raffia.writers.BasketWriter;

public abstract class StreamingProjector<T extends BasketWriter<T>> extends FilteringWriter<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> start(T target, Path path) {
    return OuterProjector.create(target.beginArray(), path);
  }

  private Projector<Basket> makeConditionalProjector(Path path) {
    return path.head().createItemProjector(path.tail());
  }

  protected FilteringWriter<T> startArray(T target, Path path, StreamingProjector<T> parent) {
    if (path.isEmpty()) {
      return InnerProjector.matchedArray(target, parent);
    }

    if (path.head().isConditional()) {
      return InnerProjector.predicateMatching(target, parent, makeConditionalProjector(path));
    }

    return InnerProjector.arrayIndexSeeking(target, path, parent);
  }

  protected FilteringWriter<T> startObject(T target, Path path, StreamingProjector<T> parent) {
    if (path.isEmpty()) {
      return InnerProjector.matchedObject(target, parent);
    }

    if (path.head().isConditional()) {
      return InnerProjector.predicateMatching(target, parent, makeConditionalProjector(path));
    }

    return InnerProjector.objectKeySeeking(target, path, parent);
  }

  public StreamingProjector(T target) {
    super(target);
  }

  @Override
  public T complete() {
    return getTarget().end();
  }

  public abstract FilteringWriter<T> advance(T newTarget);

  protected FilteringWriter<T> ignore() {
    return advance(getTarget());
  }

}
