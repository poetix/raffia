package com.codepoetics.raffia.updaters;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.projections.Projections;

import java.math.BigDecimal;

import static com.codepoetics.raffia.injections.Injections.*;
import static com.codepoetics.raffia.mappers.Mappers.compose;

public final class Updaters {

  private Updaters() {
  }

  public static Updater ofString(final Mapper<String, String> stringMapper) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return basket.isString() ? Basket.ofString(stringMapper.map(basket.asString())) : basket;
      }
    };
  }

  public static Updater ofNumber(final Mapper<BigDecimal, BigDecimal> numberMapper) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return basket.isNumber() ? Basket.ofNumber(numberMapper.map(basket.asNumber())) : basket;
      }
    };
  }

  public static Updater ofBoolean(final Mapper<Boolean, Boolean> booleanMapper) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return basket.isBoolean() ? Basket.ofBoolean(booleanMapper.map(basket.asBoolean())) : basket;
      }
    };
  }

  public static Updater ofArray(final Mapper<ArrayContents, ArrayContents> arrayContentsMapper) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return basket.mapArray(arrayContentsMapper);
      }
    };
  }

  public static Updater ofObject(final Mapper<PropertySet, PropertySet> propertySetMapper) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        return basket.mapObject(propertySetMapper);
      }
    };
  }

  public static Updater appending(final Basket arrayItem) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.plus(arrayItem);
      }
    });
  }

  public static Updater inserting(final int index, final Basket arrayItem) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.with(index, arrayItem);
      }
    });
  }

  public static Updater replacing(final int index, final Basket arrayItem) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.plus(index, arrayItem);
      }
    });
  }

  public static Updater inserting(final String key, final Basket value) {
    return ofObject(new Mapper<PropertySet, PropertySet>() {
      @Override
      public PropertySet map(PropertySet input) {
        return input.with(key, value);
      }
    });
  }

  public static Updater removing(final int index) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        return input.minus(index);
      }
    });
  }

  public static Updater removing(final String key) {
    return ofObject(new Mapper<PropertySet, PropertySet>() {
      @Override
      public PropertySet map(PropertySet input) {
        return input.minus(key);
      }
    });
  }

  public static Updater updating(final int index, final Updater itemUpdater) {
    return ofArray(new Mapper<ArrayContents, ArrayContents>() {
      @Override
      public ArrayContents map(ArrayContents input) {
        if (index >= input.size()) {
          return input;
        }
        int actual = index < 0 ? input.size() + index : index;
        return input.with(actual, itemUpdater.update(input.get(actual)));
      }
    });
  }

  public static Updater updating(final String key, final Updater valueUpdater) {
    return ofObject(new Mapper<PropertySet, PropertySet>() {
      @Override
      public PropertySet map(PropertySet input) {
        return input.containsKey(key) ? input.with(key, valueUpdater.update(input.get(key))) : input;
      }
    });
  }

}
