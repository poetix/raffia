package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.WeavingFilter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.writers.Writers;

class WeavingRewriter<T extends BasketWriter<T>> extends WeavingFilter<T> {

  static <T extends BasketWriter<T>> WeavingRewriter<T> weavingObject(T target, StreamingRewriter<T> parent, Updater updater) {
    return weaving(target, parent, updater, Writers.weaving().beginObject());
  }

  static <T extends BasketWriter<T>> WeavingRewriter<T> weavingArray(T target, StreamingRewriter<T> parent, Updater updater) {
    return weaving(target, parent, updater, Writers.weaving().beginArray());
  }

  private static <T extends BasketWriter<T>> WeavingRewriter<T> weaving(T target, StreamingRewriter<T> parent, Updater updater, BasketWeavingWriter weaver) {
    return new WeavingRewriter<>(target, parent, updater, weaver);
  }

  private final T target;
  private final Updater updater;

  private WeavingRewriter(T target, StreamingRewriter<T> parent, Updater updater, BasketWeavingWriter weaver) {
    super(parent, weaver);
    this.updater = updater;
    this.target = target;
  }

  @Override
  protected T writeToTarget(Basket woven) {
    return updater.update(woven).visit(Visitors.writingTo(target));
  }
}
