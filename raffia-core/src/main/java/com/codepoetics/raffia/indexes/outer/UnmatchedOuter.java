package com.codepoetics.raffia.indexes.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;

import java.math.BigDecimal;

abstract class UnmatchedOuter<T extends BasketWriter<T>> extends Outer<T> {

  UnmatchedOuter(T target, Visitor<Basket> updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return advance(getTarget().addNull());
  }

}
