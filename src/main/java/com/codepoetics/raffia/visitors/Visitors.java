package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.api.*;
import org.pcollections.PVector;

import java.math.BigDecimal;
import java.util.List;

public final class Visitors {

  private Visitors() {
  }

  public static final Visitor<Basket> copy = new CopyVisitor();

  public static final Visitor<Object> object = new ObjectVisitor();

  public static <I, O> Visitor<O> chain(final Visitor<I> first, final Mapper<I, Visitor<O>> second) {
    return new Visitor<O>() {
      @Override
      public O visitString(String value) {
        return second.map(first.visitString(value)).visitString(value);
      }

      @Override
      public O visitBoolean(boolean value) {
        return second.map(first.visitBoolean(value)).visitBoolean(value);
      }

      @Override
      public O visitNumber(BigDecimal value) {
        return second.map(first.visitNumber(value)).visitNumber(value);
      }

      @Override
      public O visitNull() {
        return second.map(first.visitNull()).visitNull();
      }

      @Override
      public O visitArray(PVector<Basket> items) {
        return second.map(first.visitArray(items)).visitArray(items);
      }

      @Override
      public O visitObject(PropertySet properties) {
        return second.map(first.visitObject(properties)).visitObject(properties);
      }
    };
  }
}
