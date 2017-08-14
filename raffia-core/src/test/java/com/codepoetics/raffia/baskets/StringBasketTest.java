package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  public void reportsToVisitorAsString() {
    Visitor<String> visitor = mock(Visitor.class);

    when(visitor.visitString(anyString())).thenReturn("result");

    assertEquals("result", unit.visit(visitor));

    verify(visitor).visitString("value");
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
  }

  @Test
  public void mapValue() {
    assertEquals(Basket.ofString("VALUE"), unit.mapString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.toUpperCase();
      }
    }));
  }

  @Test
  public void flatMapValue() {
    assertEquals(Basket.ofNumber(new BigDecimal("5")), unit.flatMapString(new Mapper<String, Basket>() {
      @Override
      public Basket map(String input) {
        return Basket.ofNumber(BigDecimal.valueOf(input.length())) ;
      }
    }));
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, unit.visit(Visitors.copy));
    assertEquals(unit.hashCode(), unit.visit(Visitors.copy).hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("\"value\"", unit.toString());
  }
}
