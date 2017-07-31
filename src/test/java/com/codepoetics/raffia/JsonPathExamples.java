package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.builders.BasketBuilder;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.predicates.NumberPredicates;
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
    assertThat(
        Lens.create().to("store").to("book").toAll().to("author").getAll(Projections.asString, store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkein"));
  }

  @Test
  public void allAuthors() {
    assertThat(
        Lens.create().toAny("author").getAll(Projections.asString, store),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkein"));
  }

  @Test
  public void getAllItems() {
    assertThat(
        Lens.create().to("store").toAll().toAll().getAll(store),
        contains(REES, WAUGH, MELVILLE, TOLKEIN, RED_BIKE));
  }

  @Test
  public void getAllPrices() {
    assertThat(
        Lens.create().to("store").toAny("price").getAll(Projections.asNumber, store),
        contains(
          new BigDecimal("8.95"),
          new BigDecimal("12.99"),
          new BigDecimal("8.99"),
          new BigDecimal("22.99"),
          new BigDecimal("19.95")));
  }

  @Test
  public void getThirdBook() {
    assertThat(Lens.create().toAny("book").to(2).getOne(store), equalTo(MELVILLE));
  }

  @Test
  public void getSecondToLastBook() {
    assertThat(Lens.create().toAny("book").to(-2).getOne(store), equalTo(MELVILLE));
  }

  @Test
  public void getFirstTwoBooks() {
    assertThat(
        Lens.create()
            .toAny("book")
            .to(0, 1)
            .getAll(store),
        contains(REES, WAUGH));
  }

  @Test
  public void getBooksWithIsbns() {
    assertThat(
        Lens.create()
            .toAny("book")
            .toHavingKey("isbn")
            .getAll(store),
        contains(MELVILLE, TOLKEIN));
  }

  @Test
  public void getCheapBooks() {
    Visitor<Boolean> isLessThanTen = Projections.map(Projections.asNumber, NumberPredicates.isLessThan(new BigDecimal("10")));
    Visitor<Boolean> priceIsLessThanTen = Lens.create().to("price").gettingOne(isLessThanTen);

    assertThat(
        Lens.create().toAny("book").toMatching("?(@.price < 10)", priceIsLessThanTen).getAll(store),
        contains(REES, MELVILLE));
  }

  @Test
  public void getArbitrarilyCheapBooks() {
    Visitor<BigDecimal> expensive = Lens.create().toAny("expensive").gettingOne(Projections.asNumber);

    final Mapper<BigDecimal, Visitor<List<Basket>>> priceIsLessThan = new Mapper<BigDecimal, Visitor<List<Basket>>>() {
      @Override
      public Visitor<List<Basket>> map(final BigDecimal expensive) {
        Visitor<Boolean> isCheap = Projections.map(Projections.asNumber, NumberPredicates.isLessThan(expensive));
        Visitor<Boolean> priceIsCheap = Lens.create().to("price").gettingOne(isCheap);

        return Lens.create().toAny("book").toMatching("", priceIsCheap).gettingAll();
      }
    };

    Visitor<List<Basket>> arbitrarilyCheapBooks = Visitors.chain(expensive, priceIsLessThan);

    assertThat(Lens.create().project(arbitrarilyCheapBooks, store), contains(REES, MELVILLE));
  }

  private static final Basket REES = book("reference", "Nigel Rees", "Sayings of the Century", "8.95");
  private static final Basket WAUGH = book("fiction", "Evelyn Waugh", "Sword of Honour", "12.99");
  private static final Basket MELVILLE = book("fiction", "Herman Melville", "Moby Dick", "0-553-21311-3", "8.99");
  private static final Basket TOLKEIN = book("fiction", "J. R. R. Tolkein", "The Lord of the Rings", "0-395-19395-8", "22.99");

  private static final Basket RED_BIKE = Raffia.builder().writeStartObject()
      .writeField("color", "red")
      .writeField("price", new BigDecimal("19.95"))
      .writeEndObject()
      .weave();

  private static final Basket store = Raffia.builder().writeStartObject()
    .writeKey("store")
      .writeStartObject()
        .writeArrayField("book", REES, WAUGH, MELVILLE, TOLKEIN)
        .writeArrayField("bicycle", RED_BIKE)
      .writeEndObject()
      .writeField("expensive", new BigDecimal("10"))
    .writeEndObject()
    .weave();

  private static BasketBuilder withCommon(String category, String author, String title, BasketBuilder builder) {
    return builder
          .writeField("category", category)
          .writeField("author", author)
          .writeField("title", title);
  }

  private static BasketBuilder withIsbn(String isbn, BasketBuilder writer) {
    return writer.writeField("isbn", isbn);
  }

  private static BasketBuilder withPrice(String price, BasketBuilder writer) {
    return writer.writeField("price", new BigDecimal(price));
  }

  private static Basket object(BasketBuilder basketBuilder) {
    return basketBuilder.writeEndObject().weave();
  }

  private static Basket book(String category, String author, String title, String price) {
    return object(
        withPrice(price,
            withCommon(category, author, title,
                Raffia.builder().writeStartObject())));
  }

  private static Basket book(String category, String author, String title, String isbn, String price) {
    return object(
        withPrice(price,
            withIsbn(isbn,
                withCommon(category, author, title,
                    Raffia.builder().writeStartObject()))));
  }

}
