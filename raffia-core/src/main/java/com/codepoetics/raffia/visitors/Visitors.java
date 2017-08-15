package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.writers.BasketWriter;

public final class Visitors {

  private Visitors() {
  }

  public static final Visitor<Basket> copy = new CopyVisitor();

  public static <T extends BasketWriter<T>> Visitor<T> writingTo(T writer) {
    return new WritingVisitor<>(writer);
  }

}
