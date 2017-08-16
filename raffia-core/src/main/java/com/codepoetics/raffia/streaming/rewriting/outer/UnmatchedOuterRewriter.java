package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

abstract class UnmatchedOuterRewriter<T extends BasketWriter<T>> extends OuterRewriter<T> {

  UnmatchedOuterRewriter(T target, Updater updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return advance(target.addNull());
  }

}
