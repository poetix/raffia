package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;
import java.util.List;

abstract class WeavingProjector<T extends BasketWriter<T>> extends FilteringWriter<T> {

  static <T extends BasketWriter<T>> WeavingProjector<T> weavingObject(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector) {
    return weaving(target, parent, projector, Writers.weaving().beginObject());
  }

  static <T extends BasketWriter<T>> WeavingProjector<T> weavingArray(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector) {
    return weaving(target, parent, projector, Writers.weaving().beginArray());
  }

  private static <T extends BasketWriter<T>> WeavingProjector<T> weaving(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector, BasketWeavingWriter weaver) {
    return new Container<>(target, parent, projector, weaver);
  }

  protected final BasketWeavingWriter weaver;

  WeavingProjector(T target, BasketWeavingWriter weaver) {
    super(target);
    this.weaver = weaver;
  }

  protected abstract FilteringWriter<T> withWeaver(BasketWeavingWriter weaver);

  @Override
  public FilteringWriter<T> key(String key) {
    return withWeaver(weaver.key(key));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return withWeaver(weaver.add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return withWeaver(weaver.add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return withWeaver(weaver.add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return withWeaver(weaver.addNull());
  }

  @Override
  public T complete() {
    throw new IllegalStateException("Cannot complete() while still weaving basket");
  }

  private static final class Container<T extends BasketWriter<T>> extends WeavingProjector<T> {

    private final Visitor<List<Basket>> projector;
    private final StreamingProjector<T> parent;

    Container(T target, StreamingProjector<T> parent, Visitor<List<Basket>> projector, BasketWeavingWriter weaver) {
      super(target, weaver);
      this.parent = parent;
      this.projector = projector;
    }

    @Override
    protected FilteringWriter<T> withWeaver(BasketWeavingWriter newWeaver) {
      return new Container<>(getTarget(), parent, projector, newWeaver);
    }

    private FilteringWriter<T> enter(BasketWeavingWriter newWeaver) {
      return new Contents<>(getTarget(), this, newWeaver);
    }

    @Override
    public FilteringWriter<T> beginObject() {
      return enter(weaver.beginObject());
    }

    @Override
    public FilteringWriter<T> beginArray() {
      return enter(weaver.beginArray());
    }

    @Override
    public FilteringWriter<T> end() {
      List<Basket> projected = weaver.weave().visit(projector);

      T newTarget = getTarget();
      for (Basket basket : projected) {
        newTarget = basket.visit(Visitors.writingTo(newTarget));
      }
      return parent.advance(newTarget);
    }
  }

  private static final class Contents<T extends BasketWriter<T>> extends WeavingProjector<T> {

    private final WeavingProjector<T> parent;

    Contents(T target, WeavingProjector<T> parent, BasketWeavingWriter weaver) {
      super(target, weaver);
      this.parent = parent;
    }

    @Override
    protected FilteringWriter<T> withWeaver(BasketWeavingWriter newWeaver) {
      return new Contents<>(getTarget(), parent, newWeaver);
    }

    @Override
    public FilteringWriter<T> beginObject() {
      return new Contents<>(getTarget(), this, weaver.beginObject());
    }

    @Override
    public FilteringWriter<T> beginArray() {
      return new Contents<>(getTarget(), this, weaver.beginArray());
    }

    @Override
    public FilteringWriter<T> end() {
      return parent.withWeaver(weaver.end());
    }

  }
}
