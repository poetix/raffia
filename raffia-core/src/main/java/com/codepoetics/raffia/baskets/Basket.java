package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Basket {

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

  public static Basket ofObject(Collection<ObjectEntry> entries) {
    return ofObject(PropertySet.of(entries));
  }

  public static Basket ofObject(Map<String, Basket> properties) {
    return ofObject(PropertySet.of(properties));
  }

  public static Basket ofObject(PropertySet properties) {
    return new ObjectBasket(properties);
  }

  public boolean isString() {
    return false;
  }

  private <T> T wrongType(BasketType requested) {
    throw new UnsupportedOperationException("Cannot get " + requested + " value from basket of type " + getType());
  }

  public abstract <T> T visit(Visitor<T> visitor);
  public abstract BasketType getType();
  public abstract <T> T getValue();

  public String asString() {
    return wrongType(BasketType.STRING);
  }

  public Basket mapString(Mapper<String, String> mapper) {
    return ofString(mapper.map(asString()));
  }

  public Basket flatMapString(Mapper<String, Basket> mapper) {
    return mapper.map(asString());
  }

  public boolean isNumber() {
    return false;
  }

  public BigDecimal asNumber() {
    return wrongType(BasketType.NUMBER);
  }

  public Basket mapNumber(Mapper<BigDecimal, BigDecimal> mapper) {
    return ofNumber(mapper.map(asNumber()));
  }

  public Basket flatMapNumber(Mapper<BigDecimal, Basket> mapper) {
    return mapper.map(asNumber());
  }

  public boolean isBoolean() {
    return false;
  }

  public boolean asBoolean() {
    return wrongType(BasketType.BOOLEAN);
  }

  public Basket mapBoolean(Mapper<Boolean, Boolean> mapper) {
    return ofBoolean(mapper.map(asBoolean()));
  }

  public Basket flatMapBoolean(Mapper<Boolean, Basket> mapper) {
    return mapper.map(asBoolean());
  }

  public boolean isNull() {
    return false;
  }

  public boolean isObject() {
    return false;
  }

  public PropertySet asObject() {
    return wrongType(BasketType.OBJECT);
  }

  public Basket mapObject(Mapper<PropertySet, PropertySet> mapper) {
    return ofObject(mapper.map(asObject()));
  }

  public Basket flatMapObject(Mapper<PropertySet, Basket> mapper) {
    return mapper.map(asObject());
  }

  public boolean isArray() {
    return false;
  }

  public ArrayContents asArray() {
    return wrongType(BasketType.ARRAY);
  }

  public Basket mapArray(Mapper<ArrayContents, ArrayContents> mapper) {
    return ofArray(mapper.map(asArray()));
  }

  public Basket flatMapArray(Mapper<ArrayContents, Basket> mapper) {
    return mapper.map(asArray());
  }

  private static final Mapper<Basket, String> asString = new Mapper<Basket, String>() {
    @Override
    public String map(Basket input) {
      return input.asString();
    }
  };

  private static final Mapper<Basket, BigDecimal> asNumber = new Mapper<Basket, BigDecimal>() {
    @Override
    public BigDecimal map(Basket input) {
      return input.asNumber();
    }
  };

  public List<String> asListOfString() {
    return asArray().map(asString);
  }

  public List<BigDecimal> asListOfNumber() {
    return asArray().map(asNumber);
  }

  public Basket withProperty(String key, Basket value) {
    return ofObject(asObject().with(key, value));
  }

  public Basket withoutProperty(String key) {
    return ofObject(asObject().minus(key));
  }

  public Basket withArrayItem(Basket item) {
    return ofArray(asArray().plus(item));
  }

  public Basket withArrayItem(int index, Basket item) {
    return ofArray(asArray().with(index, item));
  }

  public Basket withoutArrayItem(int index) {
    return ofArray(asArray().minus(index));
  }

  public boolean isEmpty() {
    switch (getType()) {
      case NULL:
        return true;
      case ARRAY:
        return (asArray().isEmpty());
      case OBJECT:
        return asObject().isEmpty();
      default:
        return false;
    }
  }

  public Basket getProperty(String key) {
    return asObject().get(key);
  }

  public Iterable<ObjectEntry> entries() {
    return asObject();
  }

  public Basket getItem(int index) {
    return asArray().get(index);
  }

  public Iterable<Basket> items() {
    return asArray();
  }

  public Basket mapItems(Mapper<Basket, Basket> itemMapper) {
    return ofArray(asArray().map(itemMapper));
  }

  public Basket mapItems(Visitor<Basket> itemMapper) {
    return ofArray(asArray().map(itemMapper));
  }

  public Basket flatMapItems(Mapper<Basket, List<Basket>> itemFlatMapper) {
    return ofArray(asArray().flatMap(itemFlatMapper));
  }

  public Basket flatMapItems(Visitor<List<Basket>> itemFlatMapper) {
    return ofArray(asArray().flatMap(itemFlatMapper));
  }

  public Basket mapValues(Mapper<Basket, Basket> valueMapper) {
    return ofObject(asObject().mapValues(valueMapper));
  }

  public Basket mapValues(Visitor<Basket> valueMapper) {
    return ofObject(asObject().mapValues(valueMapper));
  }

  public Basket mapEntries(Mapper<ObjectEntry, List<ObjectEntry>> entryMapper) {
    return ofObject(asObject().mapEntries(entryMapper));
  }
}
