package com.codepoetics.raffia.streaming.rewriting.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.inner.InnerRewriter;

import java.math.BigDecimal;

final class MatchedOuterRewriter<T extends BasketWriter<T>> extends OuterRewriter<T> {

  MatchedOuterRewriter(T target, Visitor<Basket> updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerRewriter.matchedObject(getTarget(), this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerRewriter.matchedArray(getTarget(), this, updater);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return updated(updater.visitString(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return updated(updater.visitNumber(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return updated(updater.visitBoolean(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return updated(updater.visitNull());
  }

}
