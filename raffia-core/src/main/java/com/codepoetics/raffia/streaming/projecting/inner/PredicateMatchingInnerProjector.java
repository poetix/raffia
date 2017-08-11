package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;
import com.codepoetics.raffia.visitors.Visitors;

import java.math.BigDecimal;
import java.util.List;

final class PredicateMatchingInnerProjector<T extends BasketWriter<T>> extends InnerProjector<T> {

  private final Visitor<List<Basket>> projector;

  PredicateMatchingInnerProjector(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector) {
    super(target, parent);
    this.projector =  projector;
  }

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    return new PredicateMatchingInnerProjector<>(newTarget, parent, projector);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return InnerProjector.matchedObject(getTarget(), this, projector);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return InnerProjector.matchedArray(getTarget(), this, projector);
  }

  @Override
  public FilteringWriter<T> key(String key) {
    return advance(getTarget().key(key));
  }

  private FilteringWriter<T> projected(List<Basket> projection) {
    T newTarget = getTarget();
    for (Basket basket : projection) {
      newTarget = basket.visit(Visitors.writingTo(newTarget));
    }
    return advance(newTarget);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return projected(projector.visitString(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return projected(projector.visitNumber(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return projected(projector.visitBoolean(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return projected(projector.visitNull());
  }
}
