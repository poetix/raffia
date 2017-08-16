package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public abstract class WeavingFilter<T extends BasketWriter<T>> implements FilteringWriter<T> {

  private int depth = 0;
  private final FilteringWriter<T> parent;
  private BasketWeavingWriter weaver;

  protected WeavingFilter(FilteringWriter<T> parent, BasketWeavingWriter weaver) {
    this.parent = parent;
    this.weaver = weaver;
  }

  @Override
  public FilteringWriter<T> key(String key) {
    weaver = weaver.key(key);
    return this;
  }

  @Override
  public FilteringWriter<T> add(String value) {
    weaver = weaver.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    weaver = weaver.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    weaver = weaver.add(value);
    return this;
  }

  @Override
  public FilteringWriter<T> addNull() {
    weaver = weaver.addNull();
    return this;
  }

  @Override
  public T complete() {
    throw new IllegalStateException("Cannot complete() while still weaving basket");
  }

  @Override
  public FilteringWriter<T> beginObject() {
    depth += 1;
    weaver = weaver.beginObject();
    return this;
  }

    @Override
    public FilteringWriter<T> beginArray() {
      depth += 1;
      weaver = weaver.beginObject();
      return this;
    }

    @Override
    public FilteringWriter<T> end() {
      weaver = weaver.end();

      return depth-- == 0 ? parent.advance(writeToTarget(weaver.weave())) : this;
    }

    protected abstract T writeToTarget(Basket woven);

  @Override
  public FilteringWriter<T> advance(T newTarget) {
    throw new IllegalStateException("Cannot advance() while weaving basket");
  }
}

