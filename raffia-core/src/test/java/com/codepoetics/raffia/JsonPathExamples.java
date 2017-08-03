package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.codepoetics.raffia.StoreExample.*;
import static com.codepoetics.raffia.lenses.Lens.lens;
import static com.codepoetics.raffia.predicates.NumberPredicates.isLessThan;
import static com.codepoetics.raffia.predicates.Predicates.hasKey;
import static com.codepoetics.raffia.projections.Projections.asNumber;
import static com.codepoetics.raffia.projections.Projections.asString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JsonPathExamples {

  @Test
  public void authorsOfAllBooks() {
    assertThat(
        lens("$.store.book[*].author").getAll(asString, store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
  }

  @Test
  public void allAuthors() {
    assertThat(
        lens("$..author").getAll(asString, store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
  }

  @Test
  public void getAllItems() {
    assertThat(
        lens("$.store.*[*]").getAll(store),
        contains(REES, WAUGH, MELVILLE, TOLKIEN, RED_BIKE));
  }

  @Test
  public void getBooksAndBicycles() {
    assertThat(
        lens("$.store['book', 'bicycle'][*]").getAll(store),
        contains(REES, WAUGH, MELVILLE, TOLKIEN, RED_BIKE));
  }

  @Test
  public void getBicyclesAndBooks() {
    assertThat(
        lens("$.store['bicycle', 'book'][*]").getAll(store),
        contains(RED_BIKE, REES, WAUGH, MELVILLE, TOLKIEN));
  }

  @Test
  public void getBicyclesAndBooksAndBalloons() {
    assertThat(
        lens("$.store['bicycle', 'book', 'balloon'][*]").getAll(store),
        contains(RED_BIKE, REES, WAUGH, MELVILLE, TOLKIEN));
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
    assertThat(lens("$..book[2 ]").getOne(store), equalTo(MELVILLE));
  }

  @Test
  public void getSecondToLastBook() {
    assertThat(lens("$..book[ -2]").getOne(store), equalTo(MELVILLE));
  }

  @Test
  public void getInexistentBook() {
    assertThat(lens("$..book[100]").getAll(store), hasSize(0));
  }

  @Test
  public void getInexistent() {
    assertThat(lens("$.gruffalo").getAll(store), hasSize(0));
  }

  @Test
  public void deepScanForInexistent() {
    assertThat(lens("$..gruffalo").getAll(store), hasSize(0));
  }

  @Test
  public void getFirstTwoBooks() {
    assertThat(
        lens("$..book[0, 1]").getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getFirstTwoBooksAndInexistentBook() {
    assertThat(
        lens("$..book[0, 100, 1]").getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getFirstTwoBooksInReverseOrder() {
    assertThat(
        lens("$..book[ 1, 0]").getAll(store),
        contains(WAUGH, REES));
  }

  @Test
  public void getBooksWithIsbns() {
    assertThat(lens("$..book[?]", hasKey("isbn")).getAll(store), contains(MELVILLE, TOLKIEN));
  }

  @Test
  public void getCheapBooks() {
    Visitor<Boolean> priceIsLessThanTen = lens("$.price").matchingNumber(isLessThan(10));

    assertThat(
        lens("$..book[?]", priceIsLessThanTen).getAll(store),
        contains(REES, MELVILLE));
  }

  @Test
  public void getArbitrarilyCheapBooks() {
    final Mapper<BigDecimal, Visitor<List<Basket>>> priceIsLessThan = new Mapper<BigDecimal, Visitor<List<Basket>>>() {
      @Override
      public Visitor<List<Basket>> map(final BigDecimal expensive) {
        Visitor<Boolean> priceIsCheap = lens("$.price").matchingNumber(isLessThan(expensive));

        return lens("$..book[?]", priceIsCheap).gettingAll();
      }
    };

    Visitor<List<Basket>> arbitrarilyCheapBooks = Visitors.chain(
        lens("$..expensive").gettingOne(asNumber), priceIsLessThan);

    assertThat(store.visit(arbitrarilyCheapBooks), contains(REES, MELVILLE));
  }

}
