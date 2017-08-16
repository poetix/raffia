package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;
import com.codepoetics.raffia.writers.BasketWriter;

final class PredicateMatchingOuterRewriter<T extends BasketWriter<T>> extends UnmatchedOuterRewriter<T> {

  PredicateMatchingOuterRewriter(T target, Updater updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerRewriter.predicateMatching(target.beginObject(), this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerRewriter.predicateMatching(target.beginArray(), this, updater);
  }

}
