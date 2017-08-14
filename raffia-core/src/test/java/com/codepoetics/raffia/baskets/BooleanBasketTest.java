package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
  public void reportsToVisitorAsBoolean() {
    Visitor<String> visitor = mock(Visitor.class);

    when(visitor.visitBoolean(any(Boolean.class))).thenReturn("result");

    assertEquals("result", unit.visit(visitor));

    verify(visitor).visitBoolean(true);
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, unit.visit(Visitors.copy));
    assertEquals(unit.hashCode(), unit.visit(Visitors.copy).hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("<true>", unit.toString());
  }
}
