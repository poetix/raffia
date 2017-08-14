package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;

abstract class WeavingRewriter<T extends BasketWriter<T>> extends FilteringWriter<T> {

  static <T extends BasketWriter<T>> WeavingRewriter<T> weavingObject(T target, StreamingRewriter<T> parent, Updater updater) {
    return weaving(target, parent, updater, Writers.weaving().beginObject());
  }

  static <T extends BasketWriter<T>> WeavingRewriter<T> weavingArray(T target, StreamingRewriter<T> parent, Updater updater) {
    return weaving(target, parent, updater, Writers.weaving().beginArray());
  }

  private static <T extends BasketWriter<T>> WeavingRewriter<T> weaving(T target, StreamingRewriter<T> parent, Updater updater, BasketWeavingWriter weaver) {
    return new Container<>(target, parent, updater, weaver);
  }

  protected final BasketWeavingWriter weaver;

  WeavingRewriter(T target, BasketWeavingWriter weaver) {
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

  private static final class Container<T extends BasketWriter<T>> extends WeavingRewriter<T> {

    private final Updater updater;
    private final StreamingRewriter<T> parent;

    Container(T target, StreamingRewriter<T> parent, Updater updater, BasketWeavingWriter weaver) {
      super(target, weaver);
      this.parent = parent;
      this.updater = updater;
    }

    @Override
    protected FilteringWriter<T> withWeaver(BasketWeavingWriter newWeaver) {
      return new Container<>(getTarget(), parent, updater, newWeaver);
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
      return parent.advance(
          updater.update(weaver.weave()).visit(Visitors.writingTo(getTarget())));
    }
  }

  private static final class Contents<T extends BasketWriter<T>> extends WeavingRewriter<T> {

    private final WeavingRewriter<T> parent;

    Contents(T target, WeavingRewriter<T> parent, BasketWeavingWriter weaver) {
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
