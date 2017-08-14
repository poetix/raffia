package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public final class Visitors {

  private Visitors() {
  }

  public static final Visitor<Basket> copy = new CopyVisitor();
  public static final Visitor<Object> object = new ObjectVisitor();

  public static <O> Mapper<Basket, O> toMapper(final Visitor<O> visitor) {
    return new Mapper<Basket, O>() {
      @Override
      public O map(Basket input) {
        return input.visit(visitor);
      }
    };
  }

  public static Updater toUpdater(final Visitor<Basket> visitor) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return basket.visit(visitor);
      }
    };
  }

  public static <T> Visitor<T> constant(final T constant) {
    return new Visitor<T>() {
      @Override
      public T visitString(String value) {
        return constant;
      }

      @Override
      public T visitBoolean(boolean value) {
        return constant;
      }

      @Override
      public T visitNumber(BigDecimal value) {
        return constant;
      }

      @Override
      public T visitNull() {
        return constant;
      }

      @Override
      public T visitArray(ArrayContents items) {
        return constant;
      }

      @Override
      public T visitObject(PropertySet properties) {
        return constant;
      }
    };
  }

  public static <T extends BasketWriter<T>> Visitor<T> writingTo(T writer) {
    return new WritingVisitor<>(writer);
  }

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
      public O visitArray(ArrayContents items) {
        return second.map(first.visitArray(items)).visitArray(items);
      }

      @Override
      public O visitObject(PropertySet properties) {
        return second.map(first.visitObject(properties)).visitObject(properties);
      }
    };
  }
}
