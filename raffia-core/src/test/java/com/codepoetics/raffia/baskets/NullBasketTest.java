package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
  public void reportsToVisitorAsNull() {
    Visitor<String> visitor = mock(Visitor.class);

    when(visitor.visitNull()).thenReturn("result");

    assertEquals("result", unit.visit(visitor));

    verify(visitor).visitNull();
  }

  @Test
  public void emptiness() {
    assertTrue(unit.isEmpty());
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, unit.visit(Visitors.copy));
    assertEquals(unit.hashCode(), unit.visit(Visitors.copy).hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("<null>", unit.toString());
  }
}
