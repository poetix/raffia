package com.codepoetics.raffia.indexes.inner;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;

abstract class WeavingWriter<T extends BasketWriter<T>> extends FilteringWriter<T> {

  static <T extends BasketWriter<T>> WeavingWriter<T> weavingObject(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return weaving(target, parent, updater, Writers.weaving().beginObject());
  }

  static <T extends BasketWriter<T>> WeavingWriter<T> weavingArray(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return weaving(target, parent, updater, Writers.weaving().beginArray());
  }

  private static <T extends BasketWriter<T>> WeavingWriter<T> weaving(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> updater, BasketWeavingWriter weaver) {
    return new Container<>(target, parent, updater, weaver);
  }

  protected final BasketWeavingWriter weaver;

  WeavingWriter(T target, BasketWeavingWriter weaver) {
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

  private static final class Container<T extends BasketWriter<T>> extends WeavingWriter<T> {

    private final MatchSeekingUpdater<T> parent;
    private final Visitor<Basket> updater;

    Container(T target, MatchSeekingUpdater<T> parent, Visitor<Basket> updater, BasketWeavingWriter weaver) {
      super(target, weaver);
      this.parent = parent;
      this.updater = updater;
    }

    @Override
    protected FilteringWriter<T> withWeaver(BasketWeavingWriter newWeaver) {
      return new Container<>(getTarget(), parent, updater, newWeaver);
    }

    private FilteringWriter<T> enter(BasketWeavingWriter newWeaver) {
      return new Contents<T>(getTarget(), this, updater, newWeaver);
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
      Basket matched = weaver.weave();
      System.out.println("Matched: " + matched);
      Basket updated = matched.visit(updater);
      System.out.println("Updated: " + updated);

      return parent.advance(
          updated.visit(Visitors.writingTo(getTarget())));
    }
  }

  private static final class Contents<T extends BasketWriter<T>> extends WeavingWriter<T> {

    private final WeavingWriter<T> parent;
    private final Visitor<Basket> updater;

    Contents(T target, WeavingWriter<T> parent, Visitor<Basket> updater, BasketWeavingWriter weaver) {
      super(target, weaver);
      this.parent = parent;
      this.updater = updater;
    }

    @Override
    protected FilteringWriter<T> withWeaver(BasketWeavingWriter newWeaver) {
      return new Contents<>(getTarget(), parent, updater, newWeaver);
    }

    @Override
    public FilteringWriter<T> beginObject() {
      return new Contents<T>(getTarget(), this, updater, weaver.beginObject());
    }

    @Override
    public FilteringWriter<T> beginArray() {
      return new Contents<T>(getTarget(), this, updater, weaver.beginArray());
    }

    @Override
    public FilteringWriter<T> end() {
      return parent.withWeaver(weaver.end());
    }

  }
}
