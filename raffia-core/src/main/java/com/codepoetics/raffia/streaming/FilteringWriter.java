package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.writers.Writers;

public abstract class FilteringWriter<T extends BasketWriter<T>> implements BasketWriter<FilteringWriter<T>> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> rewriting(Lens lens, T target, Visitor<Basket> transformer) {
    return StreamingRewriter.start(target, lens.getPath(), transformer);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> filtering(Lens lens, T target) {
    return StreamingProjector.start(target, lens.getPath());
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
