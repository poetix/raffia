package com.codepoetics.raffia.baskets;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NullBasketTest {

  private static final Basket unit = Basket.ofNull();

  @Test
  public void identifiesAsNull() {
    assertEquals(BasketType.NULL, unit.getType());

    assertFalse(unit.isString());
    assertFalse(unit.isNumber());
    assertFalse(unit.isBoolean());
    assertTrue(unit.isNull());
    assertFalse(unit.isObject());
    assertFalse(unit.isArray());
  }

  @Test
  public void emptiness() {
    assertTrue(unit.isEmpty());
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, Basket.ofNull());
    assertEquals(unit.hashCode(), Basket.ofNull().hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("<null>", unit.toString());
  }
}
