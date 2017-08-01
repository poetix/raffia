package com.codepoetics.raffia.setters;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.visitors.Visitors;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public final class Setters {

  private Setters() {
  }

  public static Visitor<Basket> toBasket(Basket value) {
    return Visitors.constant(value);
  }

  public static Visitor<Basket> toString(String value) {
    return toBasket(Baskets.ofString(value));
  }

  public static Visitor<Basket> toNumber(BigDecimal value) {
    return toBasket(Baskets.ofNumber(value));
  }

  public static Visitor<Basket> toBoolean(boolean value) {
    return toBasket(Baskets.ofBoolean(value));
  }

  public static Visitor<Basket> toNull() {
    return toBasket(Baskets.ofNull());
  }

  public static Visitor<Basket> toArray(Collection<Basket> contents) {
    return toBasket(Baskets.ofArray(contents));
  }

  public static Visitor<Basket> toArray(ArrayContents contents) {
    return toBasket(Baskets.ofArray(contents));
  }

  public static Visitor<Basket> toArray(Basket...contents) {
    return toBasket(Baskets.ofArray(contents));
  }

  public static Visitor<Basket> toObject(ObjectEntry...properties) {
    return toBasket(Baskets.ofObject(properties));
  }

  public static Visitor<Basket> toObject(Map<String, Basket> properties) {
    return toBasket(Baskets.ofObject(properties));
  }

  public static Visitor<Basket> toObject(PropertySet properties) {
    return toBasket(Baskets.ofObject(properties));
  }

}
