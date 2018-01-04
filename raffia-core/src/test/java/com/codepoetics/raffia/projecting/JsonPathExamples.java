package com.codepoetics.raffia.projecting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updaters;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.math.BigDecimal;

import static com.codepoetics.raffia.StoreExample.*;
import static com.codepoetics.raffia.lenses.Strands.strand;
import static com.codepoetics.raffia.predicates.BasketPredicates.hasKey;
import static com.codepoetics.raffia.predicates.NumberPredicates.isLessThan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JsonPathExamples {

  @Test
  public void authorsOfAllBooks() {
    assertThat(
        strand("$.store.book[*].author").getAllStrings(store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
  }

  @Test
  public void allAuthors() {
    assertThat(
        strand("$..author").getAllStrings(store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
  }

  @Test
  public void getAllItems() {
    assertThat(
        strand("$.store.*[*]").getAll(store),
        contains(REES, WAUGH, MELVILLE, TOLKIEN, RED_BIKE));
  }

  @Test
  public void getBooksAndBicycles() {
    assertThat(
        strand("$.store['book', 'bicycle'][*]").getAll(store),
        contains(REES, WAUGH, MELVILLE, TOLKIEN, RED_BIKE));
  }

  @Test
  public void getAllButLastBook() {
    assertThat(
        strand("$..book[:-1]").getAll(store),
      contains(REES, WAUGH, MELVILLE));
  }

  @Test
  public void getAllButLastTwoBooks() {
    assertThat(
        strand("$..book[0:-2]").getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getLastTwoBooks() {
    assertThat(
        strand("$..book[-2:]").getAll(store),
        contains(MELVILLE, TOLKIEN));
  }

  @Test
  public void getMiddleTwoBooks() {
    assertThat(
        strand("$..book[1:-1]").getAll(store),
        contains(WAUGH, MELVILLE));
  }

  @Test
  public void getLastThreeBooks() {
    assertThat(
        strand("$..book[1:]").getAll(store),
        contains(WAUGH, MELVILLE, TOLKIEN));
  }

  @Test
  public void getBicyclesAndBooks() {
    assertThat(
        strand("$.store['bicycle', 'book'][*]").getAll(store),
        contains(RED_BIKE, REES, WAUGH, MELVILLE, TOLKIEN));
  }

  @Test
  public void getBicyclesAndBooksAndBalloons() {
    assertThat(
        strand("$.store['bicycle', 'book', 'balloon'][*]").getAll(store),
        contains(RED_BIKE, REES, WAUGH, MELVILLE, TOLKIEN));
  }

  @Test
  public void getAllPrices() {
    assertThat(
        strand("$.store..price").getAllNumbers(store),
        contains(
          new BigDecimal("8.95"),
          new BigDecimal("12.99"),
          new BigDecimal("8.99"),
          new BigDecimal("22.99"),
          new BigDecimal("19.95")));
  }

  @Test
  public void getThirdBook() {
    assertThat(strand("$..book[2 ]").getSingle(store), equalTo(MELVILLE));
  }

  @Test
  public void getSecondToLastBook() {
    assertThat(strand("$..book[ -2]").getSingle(store), equalTo(MELVILLE));
  }

  @Test
  public void getInexistentBook() {
    assertThat(strand("$..book[100]").getAll(store), hasSize(0));
  }

  @Test
  public void getInexistent() {
    assertThat(strand("$.gruffalo").getAll(store), hasSize(0));
  }

  @Test
  public void deepScanForInexistent() {
    assertThat(strand("$..gruffalo").getAll(store), hasSize(0));
  }

  @Test
  public void getFirstTwoBooks() {
    System.out.println(strand("$..book[0, 1]").getAll(store));
    assertThat(
        strand("$..book[0, 1]").getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getFirstTwoBooksAndInexistentBook() {
    assertThat(
        strand("$..book[0, 100, 1]").getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getFirstTwoBooksInReverseOrder() {
    assertThat(
        strand("$..book[ 1, 0]").getAll(store),
        contains(WAUGH, REES));
  }

  @Test
  public void getBooksWithIsbns() {
    assertThat(strand("$..book[?]", hasKey("isbn")).getAll(store), contains(MELVILLE, TOLKIEN));
  }

  @Test
  public void getCheapBooks() {
    Function1<Basket, Boolean> priceIsLessThanTen = strand("@.price").matchesNumber(isLessThan(10));

    assertThat(
        strand("$..book[?]", priceIsLessThanTen).getAll(store),
        contains(REES, MELVILLE));
  }

  /*
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
  */

}
