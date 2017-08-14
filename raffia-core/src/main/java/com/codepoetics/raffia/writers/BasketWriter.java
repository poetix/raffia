package com.codepoetics.raffia.writers;

import java.math.BigDecimal;

public interface BasketWriter<T extends BasketWriter<T>> {

  T beginObject();
  T beginArray();

  T end();

  T key(String key);

  T add(String value);
  T add(BigDecimal value);
  T add(boolean value);
  T addNull();

}
