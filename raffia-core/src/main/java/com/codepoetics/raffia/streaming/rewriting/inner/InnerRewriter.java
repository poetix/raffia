package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;

public abstract class InnerRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  protected final StreamingRewriter<T> parent;

  protected InnerRewriter(T target, StreamingRewriter<T> parent) {
    super(target);
    this.parent = parent;
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedArray(T target, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    return WeavingRewriter.weavingArray(target, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedObject(T target, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    return WeavingRewriter.weavingObject(target, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> predicateMatching(T target, StreamingRewriter<T> parent, Visitor<Basket> itemUpdater) {
    return new PredicateMatchingInnerRewriter<>(target, parent, itemUpdater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> arrayIndexSeeking(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    return IndexSeekingInnerRewriter.seekingArrayIndex(target, path, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> objectKeySeeking(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    return IndexSeekingInnerRewriter.seekingObjectKey(target, path, parent, updater);
  }

  @Override
  public FilteringWriter<T> end() {
    return parent.advance(getTarget().end());
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(getTarget().key(key));
  }

}
