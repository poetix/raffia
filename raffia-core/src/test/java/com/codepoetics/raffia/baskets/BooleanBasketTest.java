package com.codepoetics.raffia.baskets;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BooleanBasketTest {

  private static final Basket unit = Basket.ofBoolean(true);

  @Test
  public void identifiesAsBoolean() {
    assertEquals(BasketType.BOOLEAN, unit.getType());

    assertFalse(unit.isString());
    assertFalse(unit.isNumber());
    assertTrue(unit.isBoolean());
    assertFalse(unit.isNull());
    assertFalse(unit.isObject());
    assertFalse(unit.isArray());
  }

  @Test
  public void valueIsRetrievable() {
    assertEquals(true, unit.asBoolean());
    assertEquals(true, unit.<Boolean>getValue());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void failsOnImproperRetrieval() {
    unit.asObject();
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, Basket.ofBoolean(true));
    assertEquals(unit.hashCode(), Basket.ofBoolean(true).hashCode());
    assertNotEquals(Basket.ofBoolean(true).hashCode(), Basket.ofBoolean(false).hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("<true>", unit.toString());
  }
}
