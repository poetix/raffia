package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.api.*;

import java.math.BigDecimal;

public final class Visitors {

  private Visitors() {
  }

  public static final Visitor<Basket> copy = new CopyVisitor();
  public static final Visitor<Object> object = new ObjectVisitor();

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
    return new WritingVisitor<T>(writer);
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
