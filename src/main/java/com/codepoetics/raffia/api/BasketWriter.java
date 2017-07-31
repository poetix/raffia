package com.codepoetics.raffia.api;

import java.math.BigDecimal;

public interface BasketWriter<T extends BasketWriter<T>> {

  T writeStartObject();
  T writeEndObject();

  T writeStartArray();
  T writeEndArray();

  T writeKey(String key);

  T writeString(String value);
  T writeNumber(BigDecimal value);
  T writeBoolean(boolean value);
  T writeNull();

}
