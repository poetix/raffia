package com.codepoetics.raffia.indexes.outer;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.inner.Inner;

import java.math.BigDecimal;

final class MatchedOuter<T extends BasketWriter<T>> extends Outer<T> {

  MatchedOuter(T target, Visitor<Basket> updater) {
    super(target, updater);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return Inner.matchedObject(getTarget(), this, updater);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return Inner.matchedArray(getTarget(), this, updater);
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
