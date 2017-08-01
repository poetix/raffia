package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.ArrayContents;
import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.ObjectEntry;
import com.codepoetics.raffia.api.PropertySet;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public final class Baskets {

  private Baskets() {
  }

  public static Basket ofString(String value) {
    return new StringBasket(value);
  }

  public static Basket ofNumber(BigDecimal value) {
    return new NumberBasket(value);
  }

  public static Basket ofBoolean(boolean value) {
    return value ? BooleanBasket.TRUE : BooleanBasket.FALSE;
  }

  public static Basket ofNull() {
    return NullBasket.INSTANCE;
  }

  public static Basket ofArray(Basket...entries) {
    return ofArray(Arrays.asList(entries));
  }

  public static Basket ofArray(Collection<Basket> entries) {
    return ofArray(ArrayContents.of(entries));
  }

  public static Basket ofArray(ArrayContents entries) {
    return new ArrayBasket(entries);
  }

  public static Basket ofObject(ObjectEntry...entries) {
    return ofObject(PropertySet.of(entries));
  }

  public static Basket ofObject(Map<String, Basket> properties) {
    return ofObject(PropertySet.of(properties));
  }

  public static Basket ofObject(PropertySet properties) {
    return new ObjectBasket(properties);
  }

}
