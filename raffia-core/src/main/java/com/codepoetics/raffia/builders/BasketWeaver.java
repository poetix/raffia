package com.codepoetics.raffia.builders;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.injections.Injections;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.mappers.Mappers;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class BasketWeaver implements BasketWriter<BasketWeaver> {

  public static BasketWeaver create() {
    return weavingWith(Writers.weaving());
  }

  public static BasketWeaver weavingWith(BasketWeavingWriter writer) {
    return new BasketWeaver(writer);
  }

  private final BasketWeavingWriter writer;

  private BasketWeaver(BasketWeavingWriter writer) {
    this.writer = writer;
  }

  public Basket weave() {
    return writer.weave();
  }

  private BasketWeaver with(BasketWeavingWriter writer) {
    return new BasketWeaver(writer);
  }

  @Override
  public BasketWeaver beginObject() {
    return with(writer.beginObject());
  }

  @Override
  public BasketWeaver beginArray() {
    return with(writer.beginArray());
  }

  @Override
  public BasketWeaver end() {
    return with(writer.end());
  }

  @Override
  public BasketWeaver key(String key) {
    return with(writer.key(key));
  }

  @Override
  public BasketWeaver add(String value) {
    return with(writer.add(value));
  }

  @Override
  public BasketWeaver add(BigDecimal value) {
    return with(writer.add(value));
  }

  @Override
  public BasketWeaver add(boolean value) {
    return with(writer.add(value));
  }

  @Override
  public BasketWeaver addNull() {
    return with(writer.addNull());
  }

  public BasketWeaver add(String key, String value) {
    return key(key).add(value);
  }

  public BasketWeaver add(String key, BigDecimal value) {
    return key(key).add(value);
  }

  public BasketWeaver add(String key, boolean value) {
    return key(key).add(value);
  }

  public BasketWeaver add(String key, Basket value) {
    return key(key).add(value);
  }

  public BasketWeaver array() {
    return with(writer.beginArray().end());
  }

  public BasketWeaver array(Basket firstItem, Basket...subsequentItems) {
    return array(firstItem, subsequentItems, Mappers.<Basket>id());
  }

  public BasketWeaver array(String firstItem, String...subsequentItems) {
    return array(firstItem, subsequentItems, Injections.fromString);
  }

  public BasketWeaver array(BigDecimal firstItem, BigDecimal...subsequentItems) {
    return array(firstItem, subsequentItems, Injections.fromNumber);
  }

  public BasketWeaver array(boolean firstItem, Boolean...subsequentItems) {
    return array(firstItem, subsequentItems, Injections.fromBoolean);
  }

  private <T> BasketWeaver array(T first, T[] subsequent, Mapper<T, Basket> mapper) {
    BasketWeavingWriter state = writer.beginArray();
    state = state.add(mapper.map(first));
    for (T item : subsequent) {
      state = state.add(mapper.map(item));
    }
    return with(state.end());
  }

  public BasketWeaver array(Collection<Basket> items) {
    BasketWeavingWriter state = writer.beginArray();
    for (Basket basket : items) {
      state = state.add(basket);
    }
    return with(state.end());
  }

  public BasketWeaver addArray(String key) {
    return key(key).array();
  }

  public BasketWeaver addArray(String key, Basket firstItem, Basket...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketWeaver addArray(String key, String firstItem, String...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketWeaver addArray(String key, BigDecimal firstItem, BigDecimal...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketWeaver addArray(String key, boolean firstItem, Boolean...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketWeaver add(Basket value) {
    return with(writer.add(value));
  }

  @Override
  public String toString() {
    return "Basket weaver with state: " + writer;
  }

}
