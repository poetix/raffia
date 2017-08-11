package com.codepoetics.raffia.builders;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.injections.Injections;
import com.codepoetics.raffia.mappers.Mappers;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class BasketBuilder implements BasketWriter<BasketBuilder> {

  private BasketBuilder(BasketWeavingWriter writer) {
    this.writer = writer;
  }

  public static BasketBuilder empty() {
    return new BasketBuilder(Writers.weaving());
  }

  private final BasketWeavingWriter writer;

  public Basket weave() {
    return writer.weave();
  }

  private BasketBuilder with(BasketWeavingWriter writer) {
    return new BasketBuilder(writer);
  }

  @Override
  public BasketBuilder beginObject() {
    return with(writer.beginObject());
  }

  @Override
  public BasketBuilder end() {
    return with(writer.end());
  }

  public BasketBuilder array() {
    return array(Collections.<Basket>emptyList());
  }

  public BasketBuilder array(Basket firstItem, Basket...subsequentItems) {
    return array(asList(firstItem, subsequentItems, Mappers.<Basket>id()));
  }

  public BasketBuilder array(String firstItem, String...subsequentItems) {
    return array(asList(firstItem, subsequentItems, Injections.fromString));
  }

  public BasketBuilder array(BigDecimal firstItem, BigDecimal...subsequentItems) {
    return array(asList(firstItem, subsequentItems, Injections.fromNumber));
  }

  public BasketBuilder array(boolean firstItem, Boolean...subsequentItems) {
    return array(asList(firstItem, subsequentItems, Injections.fromBoolean));
  }

  private <T> List<Basket> asList(T first, T[] subsequent, Mapper<T, Basket> mapper) {
    List<Basket> result = new ArrayList<>(subsequent.length + 1);
    result.add(mapper.map(first));
    for (T item : subsequent) {
      result.add(mapper.map(item));
    }
    return result;
  }

  public BasketBuilder array(Collection<Basket> items) {
    BasketBuilder state = beginArray();
    for (Basket basket : items) {
      state = state.add(basket);
    }
    return state.end();
  }

  public BasketBuilder add(Basket basket) {
    return with(writer.add(basket));
  }

  @Override
  public BasketBuilder beginArray() {
    return with(writer.beginArray());
  }

  public BasketBuilder add(BasketBuilder builder) {
    return add(builder.weave());
  }

  @Override
  public BasketBuilder key(String key) {
    return with(writer.key(key));
  }

  public BasketBuilder add(String key, String value) {
    return key(key).add(value);
  }

  public BasketBuilder add(String key, BigDecimal value) {
    return key(key).add(value);
  }

  public BasketBuilder add(String key, boolean value) {
    return key(key).add(value);
  }

  public BasketBuilder add(String key, Basket value) {
    return key(key).add(value);
  }

  public BasketBuilder addArray(String key) {
    return key(key).array();
  }

  public BasketBuilder addArray(String key, Basket firstItem, Basket...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder addArray(String key, String firstItem, String...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder addArray(String key, BigDecimal firstItem, BigDecimal...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder addArray(String key, boolean firstItem, Boolean...subsequent) {
    return key(key).array(firstItem, subsequent);
  }

  public BasketBuilder addNull(String key) {
    return key(key).addNull();
  }

  @Override
  public BasketBuilder add(String value) {
    return with(writer.add(value));
  }

  @Override
  public BasketBuilder add(BigDecimal value) {
    return with(writer.add(value));
  }

  @Override
  public BasketBuilder add(boolean value) {
    return with(writer.add(value));
  }

  @Override
  public BasketBuilder addNull() {
    return with(writer.addNull());
  }
}
