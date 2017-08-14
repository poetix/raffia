package com.codepoetics.raffia.updaters;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.projections.Projections;

import java.math.BigDecimal;

import static com.codepoetics.raffia.injections.Injections.*;
import static com.codepoetics.raffia.mappers.Mappers.compose;

public final class Updaters {

  private Updaters() {
  }

  public static <I, O> Visitor<Basket> from(Visitor<I> projection, Mapper<I, O> mapper, Mapper<O, Basket> injection) {
    return Projections.map(projection, compose(mapper, injection));
  }

  public static Visitor<Basket> ofString(Mapper<String, String> stringMapper) {
    return from(Projections.asString, stringMapper, fromString);
  }

  public static Visitor<Basket> ofNumber(Mapper<BigDecimal, BigDecimal> numberMapper) {
    return from(Projections.asNumber, numberMapper, fromNumber);
  }

  public static Visitor<Basket> ofBoolean(Mapper<Boolean, Boolean> booleanMapper) {
    return from(Projections.asBoolean, booleanMapper, fromBoolean);
  }

  public static Visitor<Basket> ofArray(Mapper<ArrayContents, ArrayContents> arrayContentsMapper) {
    return from(Projections.asArray, arrayContentsMapper, fromArrayContents);
  }

  public static Visitor<Basket> ofObject(Mapper<PropertySet, PropertySet> propertySetMapper) {
    return from(Projections.asObject, propertySetMapper, fromPropertySet);
  }

  public static Visitor<Basket> appending(final Basket arrayItem) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.plus(arrayItem);
      }
    });
  }

  public static Visitor<Basket> inserting(final int index, final Basket arrayItem) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.with(index, arrayItem);
      }
    });
  }

  public static Visitor<Basket> replacing(final int index, final Basket arrayItem) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.plus(index, arrayItem);
      }
    });
  }

  public static Visitor<Basket> inserting(final String key, final Basket value) {
    return ofObject(new Mapper<PropertySet, PropertySet>() {
      @Override
      public PropertySet map(PropertySet input) {
        return input.with(key, value);
      }
    });
  }

  public static Visitor<Basket> removing(final int index) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.minus(index);
      }
    });
  }

  public static Visitor<Basket> removing(final String key) {
    return ofObject(new Mapper<PropertySet, PropertySet>() {
      @Override
      public PropertySet map(PropertySet input) {
        return input.minus(key);
      }
    });
  }

  public static Visitor<Basket> updating(final int index, final Visitor<Basket> itemUpdater) {
    return Lens.lens().to(index).updating(itemUpdater);
  }

  public static Visitor<Basket> updating(String key, Visitor<Basket> itemUpdater) {
    return Lens.lens().to(key).updating(itemUpdater);
  }

}
