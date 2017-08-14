package com.codepoetics.raffia.baskets;

import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ArrayBasketTest {

  private static final ArrayContents contents = ArrayContents.of(
      Basket.ofString("value"),
      Basket.ofNumber(new BigDecimal("3.14")),
      Basket.ofBoolean(true),
      Basket.ofNull()
  );

  private static final Basket unit = Basket.ofArray(contents);

  @Test
  public void identifiesAsArray() {
    assertEquals(BasketType.ARRAY, unit.getType());

    assertFalse(unit.isString());
    assertFalse(unit.isNumber());
    assertFalse(unit.isBoolean());
    assertFalse(unit.isNull());
    assertFalse(unit.isObject());
    assertTrue(unit.isArray());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void failsOnImproperRetrieval() {
    unit.asBoolean();
  }

  @Test
  public void valueIsRetrievable() {
    assertEquals(contents, unit.asArray());

    assertEquals(contents, unit.<ArrayContents>getValue());
  }

  @Test
  public void reportsToVisitorAsObject() {
    Visitor<String> visitor = mock(Visitor.class);

    when(visitor.visitArray(any(ArrayContents.class))).thenReturn("result");

    assertEquals("result", unit.visit(visitor));

    verify(visitor).visitArray(contents);
  }

  @Test
  public void addItem() {
    ArrayContents extended = contents.plus(Basket.ofString("extra"));

    assertEquals(Basket.ofArray(extended), unit.withArrayItem(Basket.ofString("extra")));
  }

  @Test
  public void addItemAtIndex() {
    ArrayContents extended = contents.with(2, Basket.ofString("replacement"));

    assertEquals(Basket.ofArray(extended), unit.withArrayItem(2, Basket.ofString("replacement")));
  }

  @Test
  public void removeItem() {
    ArrayContents truncated = contents.minus(2);

    assertEquals(Basket.ofArray(truncated), unit.withoutArrayItem(2));
  }

  @Test
  public void emptiness() {
    assertFalse(unit.isEmpty());
    assertTrue(unit
        .withoutArrayItem(3)
        .withoutArrayItem(2)
        .withoutArrayItem(1)
        .withoutArrayItem(0)
        .isEmpty());
  }

  @Test
  public void getItem() {
    assertEquals(Basket.ofBoolean(true), unit.getItem(2));
  }

  @Test
  public void iterateOverItems() {
    assertThat(unit.items(), contains(
        Basket.ofString("value"),
        Basket.ofNumber(new BigDecimal("3.14")),
        Basket.ofBoolean(true),
        Basket.ofNull()));
  }

  @Test
  public void equalityAndHashcode() {
    assertEquals(unit, unit.visit(Visitors.copy));
    assertEquals(unit.hashCode(), unit.visit(Visitors.copy).hashCode());
  }

  @Test
  public void stringRepresentation() {
    assertEquals(contents.toString(), unit.toString());
  }
}
