package com.codepoetics.raffia.projecting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.functions.Mapper;
import com.codepoetics.raffia.functions.BasketPredicate;
import com.codepoetics.raffia.functions.Projector;
import com.codepoetics.raffia.operations.Projectors;
import org.junit.Test;

import java.math.BigDecimal;

import static com.codepoetics.raffia.StoreExample.*;
import static com.codepoetics.raffia.lenses.Lens.lens;
import static com.codepoetics.raffia.predicates.BasketPredicates.hasKey;
import static com.codepoetics.raffia.predicates.NumberPredicates.isLessThan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JsonPathExamples {

  @Test
  public void authorsOfAllBooks() {
    assertThat(
        lens("$.store.book[*].author").getAllStrings(store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
  }

  @Test
  public void allAuthors() {
    assertThat(
        lens("$..author").getAllStrings(store),
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
  public void getAllButLastBook() {
    assertThat(
        lens("$..book[:-1]").getAll(store),
      contains(REES, WAUGH, MELVILLE));
  }

  @Test
  public void getAllButLastTwoBooks() {
    assertThat(
        lens("$..book[0:-2]").getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getLastTwoBooks() {
    assertThat(
        lens("$..book[-2:]").getAll(store),
        contains(MELVILLE, TOLKIEN));
  }

  @Test
  public void getMiddleTwoBooks() {
    assertThat(
        lens("$..book[1:-1]").getAll(store),
        contains(WAUGH, MELVILLE));
  }

  @Test
  public void getLastThreeBooks() {
    assertThat(
        lens("$..book[1:]").getAll(store),
        contains(WAUGH, MELVILLE, TOLKIEN));
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
        lens("$.store..price").getAllNumbers(store),
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
    System.out.println(lens("$..book[0, 1]").getAll(store));
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
    BasketPredicate priceIsLessThanTen = lens("$.price").matchingNumber(isLessThan(10));

    assertThat(
        lens("$..book[?]", priceIsLessThanTen).getAll(store),
        contains(REES, MELVILLE));
  }

  @Test
  public void getArbitrarilyCheapBooks() {
    final Mapper<Basket, Projector<Basket>> priceIsLessThan = new Mapper<Basket, Projector<Basket>>() {
      @Override
      public Projector<Basket> map(final Basket expensive) {
        BasketPredicate priceIsCheap = lens("@.price").matchingNumber(isLessThan(expensive.asNumber()));

        return lens("$..book[?]", priceIsCheap);
      }
    };

    Projector<Basket> arbitrarilyCheapBooks = Projectors.feedback(
        lens("$..expensive"), priceIsLessThan);

    assertThat(arbitrarilyCheapBooks.project(store).asList(), contains(REES, MELVILLE));
  }

}
