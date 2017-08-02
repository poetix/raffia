package com.codepoetics.raffia.visitors;

import com.codepoetics.raffia.api.*;

import java.math.BigDecimal;

class WritingVisitor<T extends BasketWriter<T>> implements Visitor<T> {

  private final BasketWriter<T> writer;

  WritingVisitor(BasketWriter<T> writer) {
    this.writer = writer;
  }

  @Override
  public T visitString(String value) {
    return writer.add(value);
  }

  @Override
  public T visitBoolean(boolean value) {
    return writer.add(value);
  }

  @Override
  public T visitNumber(BigDecimal value) {
    return writer.add(value);
  }

  @Override
  public T visitNull() {
    return writer.addNull();
  }

  @Override
  public T visitArray(ArrayContents items) {
    T state = writer.beginArray();
    for (Basket item : items) {
      state = item.visit(new WritingVisitor<>(state));
    }
    return state.end();
  }

  @Override
  public T visitObject(PropertySet properties) {
    T state = writer.beginObject();
    for (ObjectEntry entry : properties) {
      state = state.key(entry.getKey());
      state = entry.getValue().visit(new WritingVisitor<>(state));
    }
    return state.end();
  }

}
