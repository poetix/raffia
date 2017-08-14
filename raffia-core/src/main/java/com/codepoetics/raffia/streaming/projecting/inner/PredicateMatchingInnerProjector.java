package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;
import com.codepoetics.raffia.visitors.Visitors;

import java.math.BigDecimal;
import java.util.List;

final class PredicateMatchingInnerProjector<T extends BasketWriter<T>> extends InnerProjector<T> {

  private final Projector<Basket> projector;

  PredicateMatchingInnerProjector(T target, StreamingProjector<T> parent, Projector<Basket> projector) {
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

  private FilteringWriter<T> projected(ProjectionResult<Basket> projection) {
    T newTarget = getTarget();
    for (Basket basket : projection) {
      newTarget = basket.visit(Visitors.writingTo(newTarget));
    }
    return advance(newTarget);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return projected(projector.project(Basket.ofString(value)));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return projected(projector.project(Basket.ofNumber(value)));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return projected(projector.project(Basket.ofBoolean(value)));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return projected(projector.project(Basket.ofNull()));
  }
}
