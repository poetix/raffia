package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

public abstract class StreamingProjector<T extends BasketWriter<T>> implements FilteringWriter<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> startArray(T target, Path path) {
    return ArrayClosingProjector.closing(start(target.beginArray(), path));
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> start(T target, Path path) {
    if (path.isEmpty()) {
      return matched(target);
    }

    return new StructStartSeekingProjector<>(target, path);
  }

  private static <T extends BasketWriter<T>> FilteringWriter<T> matched(T target) {
    return new MatchedProjector<>(target, null);
  }

  private static Projector<Basket> makeConditionalProjector(Path path) {
    return path.head().createItemProjector(path.tail());
  }

  static <T extends BasketWriter<T>> FilteringWriter<T> startArray(T target, Path path, FilteringWriter<T> parent) {
    if (path.isEmpty()) {
      return new MatchedProjector<T>(target, parent);
    }

    if (path.head().isConditional()) {
      return new PredicateMatchingProjector<>(target, parent, makeConditionalProjector(path));
    }

    return IndexSeekingProjector.seekingArrayIndex(target, path, parent);
  }

  static <T extends BasketWriter<T>> FilteringWriter<T> startObject(T target, Path path, FilteringWriter<T> parent) {
    if (path.isEmpty()) {
      return new MatchedProjector<>(target, parent);
    }

    if (path.head().isConditional()) {
      return new PredicateMatchingProjector<>(target, parent, makeConditionalProjector(path));
    }

    return IndexSeekingProjector.seekingObjectKey(target, path, parent);
  }

  protected T target;
  protected FilteringWriter<T> parent;

  StreamingProjector(T target, FilteringWriter<T> parent) {
    this.target = target;
    this.parent = parent;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    target = newTarget;
    return this;
  }

  FilteringWriter<T> ignore() {
    return advance(target);
  }

  @Override
  public final T complete() {
    return target;
  }

}
