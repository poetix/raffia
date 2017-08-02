package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.codepoetics.raffia.StoreExample.*;
import static com.codepoetics.raffia.lenses.Lens.lens;
import static com.codepoetics.raffia.predicates.NumberPredicates.isLessThan;
import static com.codepoetics.raffia.projections.Projections.asNumber;
import static com.codepoetics.raffia.projections.Projections.asString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class JsonPathExamples {

  @Test
  public void authorsOfAllBooks() {
    assertThat(
        lens("$.store.book[*].author").getAll(asString, store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkein"));
  }

  @Test
  public void allAuthors() {
    assertThat(
        lens("$..author").getAll(asString, store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkein"));
  }

  @Test
  public void getAllItems() {
    assertThat(
        lens("$.store.*[*]").getAll(store),
        contains(REES, WAUGH, MELVILLE, TOLKEIN, RED_BIKE));
  }

  @Test
  public void getAllPrices() {
    assertThat(
        lens("$.store..price").getAll(asNumber, store),
        contains(
          new BigDecimal("8.95"),
          new BigDecimal("12.99"),
          new BigDecimal("8.99"),
          new BigDecimal("22.99"),
          new BigDecimal("19.95")));
  }

  @Test
  public void getThirdBook() {
    assertThat(lens("$..book[2]").getOne(store), equalTo(MELVILLE));
  }

  @Test
  public void getSecondToLastBook() {
    assertThat(lens("$..book[-2]").getOne(store), equalTo(MELVILLE));
  }

  @Test
  public void getFirstTwoBooks() {
    assertThat(
        lens("$..book")
            .to(0, 1)
            .getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getBooksWithIsbns() {
    assertThat(
        lens("$..book")
            .toHavingKey("isbn")
            .getAll(store),
        contains(MELVILLE, TOLKEIN));
  }

  @Test
  public void getCheapBooks() {
    Visitor<Boolean> priceIsLessThanTen = lens("$.price").matchingNumber(isLessThan(10));

    assertThat(
        lens("$..book").toMatching(priceIsLessThanTen).getAll(store),
        contains(REES, MELVILLE));
  }

  @Test
  public void getArbitrarilyCheapBooks() {
    final Mapper<BigDecimal, Visitor<List<Basket>>> priceIsLessThan = new Mapper<BigDecimal, Visitor<List<Basket>>>() {
      @Override
      public Visitor<List<Basket>> map(final BigDecimal expensive) {
        Visitor<Boolean> priceIsCheap = lens("$.price").matchingNumber(isLessThan(expensive));

        return lens("$..book").toMatching(priceIsCheap).gettingAll();
      }
    };

    Visitor<List<Basket>> arbitrarilyCheapBooks = Visitors.chain(
        lens("$..expensive").gettingOne(asNumber), priceIsLessThan);

    assertThat(lens().project(arbitrarilyCheapBooks, store), contains(REES, MELVILLE));
  }

}
