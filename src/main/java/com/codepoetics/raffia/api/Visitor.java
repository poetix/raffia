package com.codepoetics.raffia.api;

import org.pcollections.PVector;

import java.math.BigDecimal;

public interface Visitor<T> {

  T visitString(String value);

  T visitBoolean(boolean value);

  T visitNumber(BigDecimal value);

  T visitNull();

  T visitArray(PVector<Basket> items);

  T visitObject(PropertySet properties);

}
