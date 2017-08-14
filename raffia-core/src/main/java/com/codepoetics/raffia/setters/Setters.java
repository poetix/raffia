package com.codepoetics.raffia.setters;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.ObjectEntry;
import com.codepoetics.raffia.baskets.PropertySet;
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
    return toBasket(Basket.ofString(value));
  }

  public static Visitor<Basket> toNumber(BigDecimal value) {
    return toBasket(Basket.ofNumber(value));
  }

  public static Visitor<Basket> toBoolean(boolean value) {
    return toBasket(Basket.ofBoolean(value));
  }

  public static Visitor<Basket> toNull() {
    return toBasket(Basket.ofNull());
  }

  public static Visitor<Basket> toArray(Collection<Basket> contents) {
    return toBasket(Basket.ofArray(contents));
  }

  public static Visitor<Basket> toArray(ArrayContents contents) {
    return toBasket(Basket.ofArray(contents));
  }

  public static Visitor<Basket> toArray(Basket...contents) {
    return toBasket(Basket.ofArray(contents));
  }

  public static Visitor<Basket> toObject(ObjectEntry...properties) {
    return toBasket(Basket.ofObject(properties));
  }

  public static Visitor<Basket> toObject(Map<String, Basket> properties) {
    return toBasket(Basket.ofObject(properties));
  }

  public static Visitor<Basket> toObject(PropertySet properties) {
    return toBasket(Basket.ofObject(properties));
  }

}
