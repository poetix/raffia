package com.codepoetics.raffia.streaming.projecting.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.inner.InnerProjector;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;

import java.math.BigDecimal;
import java.util.List;

final class MatchedOuterProjector<T extends BasketWriter<T>> extends OuterProjector<T> {

  MatchedOuterProjector(T target) {
    super(target);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerProjector.matchedObject(getTarget(), this);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerProjector.matchedArray(getTarget(), this);
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
