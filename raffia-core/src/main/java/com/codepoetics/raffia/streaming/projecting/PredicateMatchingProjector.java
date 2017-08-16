package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class PredicateMatchingProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  private final Projector<Basket> projector;

  public PredicateMatchingProjector(T target, FilteringWriter<T> parent, Projector<Basket> projector) {
    super(target, parent);
    this.projector =  projector;
  }

  @Override
  public FilteringWriter<T> beginObject() {
    return WeavingProjector.weavingObject(target, this, projector);
  }

  @Override
  public FilteringWriter<T> beginArray() {
    return WeavingProjector.weavingArray(target, this, projector);
  }

  @Override
  public FilteringWriter<T> end() {
    if (parent == null) {
      throw new IllegalStateException("end() called when not writing array or object");
    }

    return parent.advance(target);
  }

  @Override
  public FilteringWriter<T> key(String key) {
    throw new IllegalStateException("key() called when not writing array or object");
  }

  private FilteringWriter<T> projected(ProjectionResult<Basket> projection) {
    for (Basket basket : projection) {
      target = basket.visit(Visitors.writingTo(target));
    }
    return this;
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
