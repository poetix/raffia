package com.codepoetics.raffia.builders;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

public class BasketBuilder<T extends BasketWriter<T>> implements BasketWriter<BasketBuilder<T>> {

  public <T extends BasketWriter<T>> BasketBuilder<T> writingTo(T writer) {
    return new BasketBuilder<T>(writer);
  }

  protected BasketBuilder(T writer) {
    this.writer = writer;
  }

  private final T writer;

  public T getWriter() {
    return writer;
  }

  protected BasketBuilder<T> with(T writer) {
    return new BasketBuilder<>(writer);
  }

  @Override
  public BasketBuilder<T> beginObject() {
    return with(writer.beginObject());
  }

  @Override
  public BasketBuilder<T> end() {
    return with(writer.end());
  }

  public BasketBuilder<T> array() {
    return array(Collections.<Basket>emptyList());
  }

  public BasketBuilder<T> array(Basket firstItem, Basket...subsequentItems) {
    T state = firstItem.visit(Visitors.writingTo(writer.beginArray()));
    for (Basket item : subsequentItems) {
      state = item.visit(Visitors.writingTo(state));
    }
    return with(state.end());
  }

  public BasketBuilder<T> array(String firstItem, String...subsequentItems) {
    T state = writer.beginArray().add(firstItem);
    for (String item : subsequentItems) {
      state = state.add(item);
    }
    return with(state.end());
  }

  public BasketBuilder<T> array(BigDecimal firstItem, BigDecimal...subsequentItems) {
    T state = writer.beginArray().add(firstItem);
    for (BigDecimal item : subsequentItems) {
      state = state.add(item);
    }
    return with(state.end());
  }

  public BasketBuilder<T> array(boolean firstItem, Boolean...subsequentItems) {
    T state = writer.beginArray().add(firstItem);
    for (boolean item : subsequentItems) {
      state = state.add(item);
    }
    return with(state.end());
  }

  public BasketBuilder<T> array(Collection<Basket> items) {
    T state = writer.beginArray();
    for (Basket basket : items) {
      state = basket.visit(Visitors.writingTo(state));
    }
    return with(state.end());
  }

  @Override
  public BasketBuilder<T> beginArray() {
    return with(writer.beginArray());
  }

  @Override
  public BasketBuilder<T> key(String key) {
    return with(writer.key(key));
  }

  public BasketBuilder<T> add(String key, String value) {
    return key(key).add(value);
  }

  public BasketBuilder<T> add(String key, BigDecimal value) {
    return key(key).add(value);
  }

  public BasketBuilder<T> add(String key, boolean value) {
    return key(key).add(value);
  }

  public BasketBuilder<T> add(String key, Basket value) {
    return key(key).add(value);
  }

  public BasketBuilder<T> addArray(String key) {
    return key(key).array();
  }

  public BasketBuilder<T> addArray(String key, Basket firstItem, Basket...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder<T> addArray(String key, String firstItem, String...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder<T> addArray(String key, BigDecimal firstItem, BigDecimal...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder<T> addArray(String key, boolean firstItem, Boolean...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder<T> addNull(String key) {
    return key(key).addNull();
  }

  public BasketBuilder<T> add(Basket value) {
    return with(value.visit(Visitors.writingTo(writer)));
  }

  @Override
  public BasketBuilder<T> add(String value) {
    return with(writer.add(value));
  }

  @Override
  public BasketBuilder<T> add(BigDecimal value) {
    return with(writer.add(value));
  }

  @Override
  public BasketBuilder<T> add(boolean value) {
    return with(writer.add(value));
  }

  @Override
  public BasketBuilder<T> addNull() {
    return with(writer.addNull());
  }

  @Override
  public String toString() {
    return "Basket builder with state: " + writer;
  }
}
