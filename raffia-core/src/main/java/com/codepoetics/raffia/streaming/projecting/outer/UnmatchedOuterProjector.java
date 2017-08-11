package com.codepoetics.raffia.streaming.projecting.outer;

import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.streaming.FilteringWriter;

import java.math.BigDecimal;

abstract class UnmatchedOuterProjector<T extends BasketWriter<T>> extends OuterProjector<T> {

  UnmatchedOuterProjector(T target) {
    super(target);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return ignore();
  }

  @Override
  public FilteringWriter<T> addNull() {
    return ignore();
  }

}
