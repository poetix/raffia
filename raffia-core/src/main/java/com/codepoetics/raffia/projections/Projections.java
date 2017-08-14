package com.codepoetics.raffia.projections;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class Projections {

  private Projections() {
  }

  public static final Projector<String> asString = new Projector<String>() {
    @Override
    public ProjectionResult<String> project(Basket basket) {
      return basket.isString() ? ProjectionResult.ofSingle(basket.asString()) : ProjectionResult.<String>empty();
    }
  };

  public static final Projector<Boolean> asBoolean = new Projector<Boolean>() {
    @Override
    public ProjectionResult<Boolean> project(Basket basket) {
      return basket.isBoolean() ? ProjectionResult.ofSingle(basket.asBoolean()) : ProjectionResult.<Boolean>empty();
    }
  };

  public static final Projector<BigDecimal> asNumber = new Projector<BigDecimal>() {
    @Override
    public ProjectionResult<BigDecimal> project(Basket basket) {
      return basket.isNumber() ? ProjectionResult.ofSingle(basket.asNumber()) : ProjectionResult.<BigDecimal>empty();
    }
  };

  public static final Projector<Void> asNull = new Projector<Void>() {
    @Override
    public ProjectionResult<Void> project(Basket basket) {
      return basket.isNull() ? ProjectionResult.ofSingle(basket.asNull()) : ProjectionResult.<Void>empty();
    }
  };

  public static final Projector<ArrayContents> asArray = new Projector<ArrayContents>() {
    @Override
    public ProjectionResult<ArrayContents> project(Basket basket) {
      return basket.isArray() ? ProjectionResult.ofSingle(basket.asArray()) : ProjectionResult.<ArrayContents>empty();
    }
  };

  public static final Projector<PropertySet> asObject = new Projector<PropertySet>() {
    @Override
    public ProjectionResult<PropertySet> project(Basket basket) {
      return basket.isObject() ? ProjectionResult.ofSingle(basket.asObject()) : ProjectionResult.<PropertySet>empty();
    }
  };

  public static <T> Projector<T> atIndex(final int index, final Projector<T> itemProjection) {
    return new Projector<T>() {
      @Override
      public ProjectionResult<T> project(Basket basket) {
        if (!basket.isArray()) {
          return ProjectionResult.empty();
        }

        ArrayContents contents = basket.asArray();
        int actual = index < 0 ? contents.size() - index : index;
        if (actual < 0 || actual >= contents.size()) {
          return ProjectionResult.empty();
        }
        return itemProjection.project(contents.get(actual));
      }
    };
  }

  public static <T> Projector<T> atKey(final String key, final Projector<T> itemProjection) {
    return new Projector<T>() {
      @Override
      public ProjectionResult<T> project(Basket basket) {
        if (basket.isObject()) {
          return ProjectionResult.empty();
        }

        PropertySet properties = basket.asObject();
        if (!properties.containsKey(key)) {
          return ProjectionResult.empty();
        }

        return itemProjection.project(properties.get(key));
      }
    };
  }

}
