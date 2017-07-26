package com.codepoetics.raffia;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.paths.Paths;
import com.codepoetics.raffia.predicates.IndexValuePredicates;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class JsonPathExamples {

  @Test
  public void authorsOfAllBooks() {
    Path author = Paths.create(
        Paths.toObjectProperty("store"),
        Paths.toObjectProperty("book"),
        Paths.toArrayWildcard(),
        Paths.toObjectProperty("author")
    );

    Visitor<List<String>> authors = Paths.projectWith(author, Projections.listOf(Projections.asString));

    assertThat(store.visit(authors), contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkein"));
  }

  @Test
  public void allAuthors() {
    Path author = Paths.create(
        Paths.toDeepScanObjectProperty("author")
    );

    Visitor<List<String>> authors = Paths.projectWith(author, Projections.listOf(Projections.asString));

    assertThat(store.visit(authors), contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkein"));
  }

  @Test
  public void getAllItems() {
    Path allThings = Paths.create(
        Paths.toObjectProperty("store"),
        Paths.toObjectWildcard(),
        Paths.toArrayWildcard()
    );

    Visitor<List<Basket>> allItems = Paths.projectWith(allThings, Projections.listOf(Visitors.copy));

    assertThat(store.visit(allItems), contains(REES, WAUGH, MELVILLE, TOLKEIN, RED_BIKE));
  }

  @Test
  public void getAllPrices() {
    Path allPrices = Paths.create(
        Paths.toObjectProperty("store"),
        Paths.toDeepScanObjectProperty("price")
    );

    Visitor<List<BigDecimal>> prices = Paths.projectWith(allPrices, Projections.listOf(Projections.asNumber));

    assertThat(store.visit(prices), contains(
        new BigDecimal("8.95"),
        new BigDecimal("12.99"),
        new BigDecimal("8.99"),
        new BigDecimal("22.99"),
        new BigDecimal("19.95")));
  }

  @Test
  public void getThirdBook() {
    Path bookAtIndex2 = Paths.create(
        Paths.toDeepScanObjectProperty("book"),
        Paths.toArrayIndex(2)
    );

    Visitor<Basket> thirdBook = Projections.firstOf(Paths.projectWith(bookAtIndex2, Projections.listOf(Visitors.copy)));

    assertThat(store.visit(thirdBook), equalTo(MELVILLE));
  }

  @Test
  public void getSecondToLastBook() {
    Path bookAtIndexMinus2 = Paths.create(
        Paths.toDeepScanObjectProperty("book"),
        Paths.toArrayIndex(-2)
    );

    Visitor<Basket> secondToLastBook = Projections.firstOf(Paths.projectWith(bookAtIndexMinus2, Projections.listOf(Visitors.copy)));

    assertThat(store.visit(secondToLastBook), equalTo(MELVILLE));
  }
  @Test
  public void getFirstTwoBooks() {
    Path firstTwoBooks = Paths.create(
      Paths.toDeepScanObjectProperty("book"),
      Paths.toArrayIndices(0, 1)
    );

    Visitor<List<Basket>> visitor = Paths.projectWith(firstTwoBooks, Projections.listOf(Visitors.copy));

    assertThat(store.visit(visitor), contains(REES, WAUGH));
  }

  @Test
  public void getBooksWithIsbns() {
    Path booksWithIsbns = Paths.create(
        Paths.toDeepScanObjectProperty("book"),
        Paths.toArrayIndex("?[@.isbn]", IndexValuePredicates.valueMatches(Predicates.isObjectWithKey("isbn")))
    );

    Visitor<List<Basket>> visitor = Paths.projectWith(booksWithIsbns, Projections.listOf(Visitors.copy));

    assertThat(store.visit(visitor), contains(MELVILLE, TOLKEIN));
  }

  @Test
  public void getCheapBooks() {
    Visitor<Boolean> priceIsLessThanTen = Projections.map(
        Projections.atKey("price", Projections.asNumber),
        NumberPredicates.isLessThan(new BigDecimal("10")));

    Path cheapBooks = Paths.create(
        Paths.toDeepScanObjectProperty("book"),
        Paths.toArrayIndex("?[@.price < 10]", IndexValuePredicates.valueMatches(priceIsLessThanTen))
    );

    Visitor<List<Basket>> visitor = Paths.projectWith(cheapBooks, Projections.listOf(Visitors.copy));

    assertThat(store.visit(visitor), contains(REES, MELVILLE));
  }

  @Test
  public void getArbitrarilyCheapBooks() {
    Path definitionOfExpensive = Paths.create(Paths.toDeepScanObjectProperty("expensive"));

    final Visitor<BigDecimal> priceCutoff = Projections.firstOf(Paths.projectWith(definitionOfExpensive, Projections.listOf(Projections.asNumber)));

    final Mapper<BigDecimal, Visitor<List<Basket>>> priceIsLessThan = new Mapper<BigDecimal, Visitor<List<Basket>>>() {
      @Override
      public Visitor<List<Basket>> map(final BigDecimal expensive) {
        Visitor<Boolean> isInexpensive = Projections.map(
            Projections.atKey("price", Projections.asNumber),
            NumberPredicates.isLessThan(expensive));

        Path cheapBooks = Paths.create(
            Paths.toDeepScanObjectProperty("book"),
            Paths.toArrayIndex("?[@.price < $..expensive]", IndexValuePredicates.valueMatches(isInexpensive))
        );

        return Paths.projectWith(cheapBooks, Projections.listOf(Visitors.copy));
      }
    };

    Visitor<List<Basket>> arbitrarilyCheapBooks = Visitors.chain(priceCutoff, priceIsLessThan);

    assertThat(store.visit(arbitrarilyCheapBooks), contains(REES, MELVILLE));
  }

  private static final Basket REES = book("reference", "Nigel Rees", "Sayings of the Century", "8.95");
  private static final Basket WAUGH = book("fiction", "Evelyn Waugh", "Sword of Honour", "12.99");
  private static final Basket MELVILLE = book("fiction", "Herman Melville", "Moby Dick", "0-553-21311-3", "8.99");
  private static final Basket TOLKEIN = book("fiction", "J. R. R. Tolkein", "The Lord of the Rings", "0-395-19395-8", "22.99");

  private static final Basket RED_BIKE = Baskets.ofObject(
      ObjectEntry.of("color", Baskets.ofString("red")),
      ObjectEntry.of("price", Baskets.ofNumber(new BigDecimal("19.95")))
  );

  private static final Basket store = Baskets.ofObject(
      ObjectEntry.of("store", Baskets.ofObject(
          ObjectEntry.of("book", Baskets.ofArray(REES, WAUGH, MELVILLE, TOLKEIN)),
          ObjectEntry.of("bicycle", Baskets.ofArray(RED_BIKE))
      )),
      ObjectEntry.of("expensive", Baskets.ofNumber(new BigDecimal("10")))
  );

  private static Basket book(String category, String author, String title, String price) {
    return Baskets.ofObject(
        ObjectEntry.of("category", Baskets.ofString(category)),
        ObjectEntry.of("author", Baskets.ofString(author)),
        ObjectEntry.of("title", Baskets.ofString(title)),
        ObjectEntry.of("price", Baskets.ofNumber(new BigDecimal(price)))
    );
  }

  private static Basket book(String category, String author, String title, String isbn, String price) {
    return Baskets.ofObject(
        ObjectEntry.of("category", Baskets.ofString(category)),
        ObjectEntry.of("author", Baskets.ofString(author)),
        ObjectEntry.of("title", Baskets.ofString(title)),
        ObjectEntry.of("isbn", Baskets.ofString(isbn)),
        ObjectEntry.of("price", Baskets.ofNumber(new BigDecimal(price)))
    );
  }

}
