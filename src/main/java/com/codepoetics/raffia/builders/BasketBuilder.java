package com.codepoetics.raffia.builders;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.BasketWriter;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

public final class BasketBuilder implements BasketWriter<BasketBuilder> {

  public BasketBuilder(BasketWeavingWriter writer) {
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
  public BasketBuilder writeStartObject() {
    return with(writer.writeStartObject());
  }

  @Override
  public BasketBuilder writeEndObject() {
    return with(writer.writeEndObject());
  }


  public BasketBuilder writeArray(Basket...items) {
    return writeArray(Arrays.asList(items));
  }

  public BasketBuilder writeArray(Collection<Basket> items) {
    BasketBuilder state = writeStartArray();
    for (Basket basket : items) {
      state = state.writeBasket(basket);
    }
    return state.writeEndArray();
  }

  public BasketBuilder writeBasket(Basket basket) {
    return with(writer.writeBasket(basket));
  }

  @Override
  public BasketBuilder writeStartArray() {
    return with(writer.writeStartArray());
  }

  @Override
  public BasketBuilder writeEndArray() {
    return with(writer.writeEndArray());
  }

  public BasketBuilder write(BasketBuilder builder) {
    return writeBasket(builder.weave());
  }

  @Override
  public BasketBuilder writeKey(String key) {
    return with(writer.writeKey(key));
  }

  public BasketBuilder writeField(String key, String value) {
    return writeKey(key).writeString(value);
  }

  public BasketBuilder writeField(String key, BigDecimal value) {
    return writeKey(key).writeNumber(value);
  }

  public BasketBuilder writeField(String key, boolean value) {
    return writeKey(key).writeBoolean(value);
  }

  public BasketBuilder writeField(String key, Basket value) {
    return writeKey(key).writeBasket(value);
  }

  public BasketBuilder writeArrayField(String key, Basket...items) {
    return writeKey(key).writeArray(items);
  }

  public BasketBuilder writeNullField(String key) {
    return writeKey(key).writeNull();
  }

  @Override
  public BasketBuilder writeString(String value) {
    return with(writer.writeString(value));
  }

  @Override
  public BasketBuilder writeNumber(BigDecimal value) {
    return with(writer.writeNumber(value));
  }

  @Override
  public BasketBuilder writeBoolean(boolean value) {
    return with(writer.writeBoolean(value));
  }

  @Override
  public BasketBuilder writeNull() {
    return with(writer.writeNull());
  }
}
