package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.api.*;
import org.pcollections.PVector;

import java.math.BigDecimal;

class WritingVisitor<T extends BasketWriter<T>> implements Visitor<T> {

  private final BasketWriter<T> writer;

  WritingVisitor(BasketWriter<T> writer) {
    this.writer = writer;
  }

  @Override
  public T visitString(String value) {
    return writer.writeString(value);
  }

  @Override
  public T visitBoolean(boolean value) {
    return writer.writeBoolean(value);
  }

  @Override
  public T visitNumber(BigDecimal value) {
    return writer.writeNumber(value);
  }

  @Override
  public T visitNull() {
    return writer.writeNull();
  }

  @Override
  public T visitArray(PVector<Basket> items) {
    T state = writer.writeStartArray();
    for (Basket item : items) {
      state = item.visit(new WritingVisitor<>(state));
    }
    return state.writeEndArray();
  }

  @Override
  public T visitObject(PropertySet properties) {
    T state = writer.writeStartObject();
    for (ObjectEntry entry : properties) {
      state = state.writeKey(entry.getKey());
      state = entry.getValue().visit(new WritingVisitor<>(state));
    }
    return state.writeEndObject();
  }

}
