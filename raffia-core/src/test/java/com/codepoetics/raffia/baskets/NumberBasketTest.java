package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NumberBasketTest {

  private static final Basket unit = Basket.ofNumber(new BigDecimal("3.14"));

  @Test
  public void identifiesAsNumber() {
    assertEquals(BasketType.NUMBER, unit.getType());

    assertFalse(unit.isString());
    assertTrue(unit.isNumber());
    assertFalse(unit.isBoolean());
    assertFalse(unit.isNull());
    assertFalse(unit.isObject());
    assertFalse(unit.isArray());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void failsOnImproperRetrieval() {
    unit.asString();
  }

  @Test
  public void valueIsRetrievable() {
    assertEquals(new BigDecimal("3.14"), unit.asNumber());
    assertEquals(new BigDecimal("3.14"), unit.<BigDecimal>getValue());
  }

  @Test
  public void reportsToVisitorAsNumber() {
    Visitor<String> visitor = mock(Visitor.class);

    when(visitor.visitNumber(any(BigDecimal.class))).thenReturn("result");

    assertEquals("result", unit.visit(visitor));

    verify(visitor).visitNumber(new BigDecimal("3.14"));
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
  }

  @Test
  public void mapValue() {
    assertEquals(Basket.ofNumber(new BigDecimal("6.28")), unit.mapNumber(new Mapper<BigDecimal, BigDecimal>() {
      @Override
      public BigDecimal map(BigDecimal input) {
        return input.multiply(new BigDecimal("2"));
      }
    }));
  }

  @Test
  public void flatMapValue() {
    assertEquals(Basket.ofString("3.14"), unit.flatMapNumber(new Mapper<BigDecimal, Basket>() {
      @Override
      public Basket map(BigDecimal input) {
        return Basket.ofString(input.toString());
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
    assertEquals("<3.14>", unit.toString());
  }
}
