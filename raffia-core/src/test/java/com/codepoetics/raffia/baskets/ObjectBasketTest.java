package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.functions.Mapper;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjectBasketTest {

  private static final PropertySet properties = PropertySet.of(
      ObjectEntry.of("string", Basket.ofString("value")),
      ObjectEntry.of("number", Basket.ofNumber(new BigDecimal("3.14"))),
      ObjectEntry.of("boolean", Basket.ofBoolean(true)),
      ObjectEntry.of("null", Basket.ofNull())
  );

  private static final Basket unit = Basket.ofObject(properties);

  @Test
  public void identifiesAsObject() {
    assertEquals(BasketType.OBJECT, unit.getType());

    assertFalse(unit.isString());
    assertFalse(unit.isNumber());
    assertFalse(unit.isBoolean());
    assertFalse(unit.isNull());
    assertTrue(unit.isObject());
    assertFalse(unit.isArray());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void failsOnImproperRetrieval() {
    unit.asArray();
  }

  @Test
  public void valueIsRetrievable() {
    assertEquals(properties, unit.asObject());

    assertEquals(properties, unit.<PropertySet>getValue());
  }

  @Test
  public void getProperty() {
    assertEquals(Basket.ofString("value"), unit.getProperty("string"));
  }

  @Test
  public void addProperty() {
    PropertySet extended = properties.with("extra", Basket.ofString("extra"));

    assertEquals(Basket.ofObject(extended), unit.withProperty("extra", Basket.ofString("extra")));
  }

  @Test
  public void removeProperty() {
    PropertySet truncated = properties.minus("number");

    assertEquals(Basket.ofObject(truncated), unit.withoutProperty("number"));
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
    assertTrue(unit
        .withoutProperty("string")
        .withoutProperty("number")
        .withoutProperty("boolean")
        .withoutProperty("null")
        .isEmpty());
  }

  @Test
  public void mapValues() {
    assertEquals(Basket.ofObject(
        ObjectEntry.of("string", Basket.ofString("value")),
        ObjectEntry.of("number", Basket.ofString("3.14")),
        ObjectEntry.of("boolean", Basket.ofString("true")),
        ObjectEntry.of("null", Basket.ofString("null"))
    ), unit.mapValues(new Mapper<Basket, Basket>() {
      @Override
      public Basket map(Basket input) {
        return Basket.ofString(Objects.toString(input.getValue()));
      }
    }));
  }

  @Test
  public void mapEntries() {
    assertEquals(unit.withoutProperty("null"), unit.mapEntries(new Mapper<ObjectEntry, List<ObjectEntry>>() {
      @Override
      public List<ObjectEntry> map(ObjectEntry input) {
        return input.getValue().isNull()
            ? Collections.<ObjectEntry>emptyList()
            : Collections.singletonList(input);
      }
    }));
  }

  @Test
  public void flatMap() {
    assertEquals(Basket.ofNumber(BigDecimal.valueOf(4)),
        unit.flatMapObject(new Mapper<PropertySet, Basket>() {
      @Override
      public Basket map(PropertySet input) {
        return Basket.ofNumber(BigDecimal.valueOf(input.size()));
      }
    }));
  }

  @Test
  public void iterateOverEntries() {
    assertThat(unit.entries(), contains(
        ObjectEntry.of("string", Basket.ofString("value")),
        ObjectEntry.of("number", Basket.ofNumber(new BigDecimal("3.14"))),
        ObjectEntry.of("boolean", Basket.ofBoolean(true)),
        ObjectEntry.of("null", Basket.ofNull())
    ));
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, Basket.ofObject(properties));
    assertEquals(unit.hashCode(), Basket.ofObject(properties).hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals(properties.toString(), unit.toString());
  }
}
