package com.codepoetics.raffia.baskets;

import kotlin.jvm.functions.Function1;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringBasketTest {

  private static final Basket unit = Basket.ofString("value");

  @Test
  public void identifiesAsString() {
    assertEquals(BasketType.STRING, unit.getType());

    assertTrue(unit.isString());
    assertFalse(unit.isNumber());
    assertFalse(unit.isBoolean());
    assertFalse(unit.isNull());
    assertFalse(unit.isObject());
    assertFalse(unit.isArray());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void failsOnImproperRetrieval() {
    unit.asNumber();
  }

  @Test
  public void valueIsRetrievable() {
    assertEquals("value", unit.asString());
    assertEquals("value", unit.<String>getValue());
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
  }

  @Test
  public void mapValue() {
    assertEquals(Basket.ofString("VALUE"), unit.mapString(new Function1<String, String>() {
      @Override
      public String invoke(String input) {
        return input.toUpperCase();
      }
    }));
  }

  @Test
  public void flatMapValue() {
    assertEquals(Basket.ofNumber(new BigDecimal("5")), unit.flatMapString(new Function1<String, Basket>() {
      @Override
      public Basket invoke(String input) {
        return Basket.ofNumber(BigDecimal.valueOf(input.length())) ;
      }
    }));
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, Basket.ofString("value"));
    assertEquals(unit.hashCode(), Basket.ofString("value").hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("\"value\"", unit.toString());
  }
}
