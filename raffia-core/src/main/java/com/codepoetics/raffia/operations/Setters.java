package com.codepoetics.raffia.operations;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.ObjectEntry;
import com.codepoetics.raffia.baskets.PropertySet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public final class Setters {

  private Setters() {
  }

  public static Updater toBasket(Basket value) {
    return Updaters.toConstant(value);
  }

  public static Updater toString(String value) {
    return toBasket(Basket.ofString(value));
  }

  public static Updater toNumber(BigDecimal value) {
    return toBasket(Basket.ofNumber(value));
  }

  public static Updater toBoolean(boolean value) {
    return toBasket(Basket.ofBoolean(value));
  }

  public static Updater toNull() {
    return toBasket(Basket.ofNull());
  }

  public static Updater toArray(Collection<Basket> contents) {
    return toBasket(Basket.ofArray(contents));
  }

  public static Updater toArray(ArrayContents contents) {
    return toBasket(Basket.ofArray(contents));
  }

  public static Updater toArray(Basket...contents) {
    return toBasket(Basket.ofArray(contents));
  }

  public static Updater toObject(ObjectEntry...properties) {
    return toBasket(Basket.ofObject(properties));
  }

  public static Updater toObject(Map<String, Basket> properties) {
    return toBasket(Basket.ofObject(properties));
  }

  public static Updater toObject(PropertySet properties) {
    return toBasket(Basket.ofObject(properties));
  }

}
