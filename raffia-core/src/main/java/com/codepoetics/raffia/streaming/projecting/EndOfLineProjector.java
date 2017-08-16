package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.streaming.EndOfLineFilter;
import com.codepoetics.raffia.writers.BasketWriter;

final class EndOfLineProjector<T extends BasketWriter<T>> extends EndOfLineFilter<T> {

  private final T target;

  protected EndOfLineProjector(T target) {
    this.target = target;
  }

  @Override
  public T complete() {
    return target.end();
  }

}
