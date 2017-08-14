package com.codepoetics.raffia.baskets;

import java.math.BigDecimal;

public interface Visitor<T> {

  T visitString(String value);

  T visitBoolean(boolean value);

  T visitNumber(BigDecimal value);

  T visitNull();

  T visitArray(ArrayContents items);

  T visitObject(PropertySet properties);

}
