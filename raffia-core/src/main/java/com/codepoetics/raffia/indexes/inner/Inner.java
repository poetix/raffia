package com.codepoetics.raffia.indexes.inner;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;

public abstract class Inner<T extends BasketWriter<T>> extends MatchSeekingUpdater<T> {

  protected final MatchSeekingUpdater<T> parent;

  protected Inner(T target, MatchSeekingUpdater<T> parent) {
    super(target);
    this.parent = parent;
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedArray(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return WeavingWriter.weavingArray(target, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> matchedObject(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return WeavingWriter.weavingObject(target, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> predicateMatching(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> itemUpdater) {
    return new PredicateMatchingInner<>(target, parent, itemUpdater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> arrayIndexSeeking(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return IndexSeekingInner.seekingArrayIndex(target, path, parent, updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> objectKeySeeking(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return IndexSeekingInner.seekingObjectKey(target, path, parent, updater);
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
