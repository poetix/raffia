package com.codepoetics.raffia.injections;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.baskets.PropertySet;

import java.math.BigDecimal;

public final class Injections {

  private Injections() {
  }

  public static final Mapper<String, Basket> fromString = new Mapper<String, Basket>() {
    @Override
    public Basket map(String input) {
      return Basket.ofString(input);
    }
  };
  public static final Mapper<BigDecimal, Basket> fromNumber = new Mapper<BigDecimal, Basket>() {
    @Override
    public Basket map(BigDecimal input) {
      return Basket.ofNumber(input);
    }
  };
  public static final Mapper<Boolean, Basket> fromBoolean = new Mapper<Boolean, Basket>() {
    @Override
    public Basket map(Boolean input) {
      return Basket.ofBoolean(input);
    }
  };

  public static final Mapper<ArrayContents, Basket> fromArrayContents = new Mapper<ArrayContents, Basket>() {
    @Override
    public Basket map(ArrayContents input) {
      return Basket.ofArray(input);
    }
  };

  public static final Mapper<PropertySet, Basket> fromPropertySet = new Mapper<PropertySet, Basket>() {
    @Override
    public Basket map(PropertySet input) {
      return Basket.ofObject(input);
    }
  };

}
