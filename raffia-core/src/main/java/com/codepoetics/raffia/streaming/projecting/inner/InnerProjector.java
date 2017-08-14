package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;

import java.util.List;

public abstract class InnerProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  protected final StreamingProjector<T> parent;

  protected InnerProjector(T target, StreamingProjector<T> parent) {
    super(target);
    this.parent = parent;
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedArray(T target, StreamingProjector<T> parent) {
    return new MatchedInnerProjector<>(target.beginArray(), parent);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedObject(T target, StreamingProjector<T> parent) {
    return new MatchedInnerProjector<>(target.beginObject(), parent);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedArray(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector) {
    return WeavingProjector.weavingArray(target, parent, projector);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedObject(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector) {
    return WeavingProjector.weavingObject(target, parent, projector);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> predicateMatching(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector) {
    return new PredicateMatchingInnerProjector<>(target, parent, projector);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> arrayIndexSeeking(T target, Path path, StreamingProjector<T> parent) {
    return IndexSeekingInnerProjector.seekingArrayIndex(target, path, parent);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> objectKeySeeking(T target, Path path, StreamingProjector<T> parent) {
    return IndexSeekingInnerProjector.seekingObjectKey(target, path, parent);
  }

  @Override
  public FilteringWriter<T> end() {
    return parent.advance(getTarget());
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(getTarget());
  }

}
