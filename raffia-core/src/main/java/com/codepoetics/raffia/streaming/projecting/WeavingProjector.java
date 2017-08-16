package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.WeavingFilter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.writers.Writers;

class WeavingProjector<T extends BasketWriter<T>> extends WeavingFilter<T> {

  static <T extends BasketWriter<T>> WeavingFilter<T> weavingObject(T target, FilteringWriter<T> parent, Projector<Basket> projector) {
    return weaving(target, parent, projector, Writers.weaving().beginObject());
  }

  static <T extends BasketWriter<T>> WeavingFilter<T> weavingArray(T target, FilteringWriter<T> parent, Projector<Basket> projector) {
    return weaving(target, parent, projector, Writers.weaving().beginArray());
  }

  private static <T extends BasketWriter<T>> WeavingFilter<T> weaving(T target, FilteringWriter<T> parent, Projector<Basket> projector, BasketWeavingWriter weaver) {
    return new WeavingProjector<>(target, parent, projector, weaver);
  }

  private final T target;
  private final Projector<Basket> projector;

  WeavingProjector(T target, FilteringWriter<T> parent, Projector<Basket> projector, BasketWeavingWriter weaver) {
    super(parent, weaver);
    this.target = target;
    this.projector = projector;
  }

  @Override
  protected T writeToTarget(Basket woven) {
    ProjectionResult<Basket> projected = projector.project(woven);

    T newTarget = target;
    for (Basket basket : projected) {
      newTarget = basket.visit(Visitors.writingTo(newTarget));
    }

    return newTarget;
  }
}
