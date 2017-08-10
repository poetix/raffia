package com.codepoetics.raffia.indexes;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.writers.Writers;

public abstract class FilteringWriter<T extends BasketWriter<T>> implements BasketWriter<FilteringWriter<T>> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> rewriting(Lens lens, T target, Visitor<Basket> transformer) {
    return MatchSeekingUpdater.start(target, lens.getPath(), transformer);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> filtering(Lens lens, T target) {
    throw new UnsupportedOperationException();
  }

  public static FilteringWriter<BasketWeavingWriter> projecting(Lens lens) {
    return filtering(lens, Writers.weaving());
  }

  private final T target;

  public FilteringWriter(T target) {
    this.target = target;
  }

  protected T getTarget() {
    return target;
  }

  public abstract T complete();

}
