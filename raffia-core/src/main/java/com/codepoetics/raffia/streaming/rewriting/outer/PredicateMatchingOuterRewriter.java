package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;

final class PredicateMatchingOuterRewriter<T extends BasketWriter<T>> extends UnmatchedOuterRewriter<T> {

  PredicateMatchingOuterRewriter(T target, Visitor<Basket> updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerRewriter.predicateMatching(getTarget().beginObject(), this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerRewriter.predicateMatching(getTarget().beginArray(), this, updater);
  }

}
