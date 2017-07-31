package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.builders.BasketBuilder;

import java.math.BigDecimal;

public class StoreExample {

  public static final Basket REES = book("reference", "Nigel Rees", "Sayings of the Century", "8.95");
  public static final Basket WAUGH = book("fiction", "Evelyn Waugh", "Sword of Honour", "12.99");
  public static final Basket MELVILLE = book("fiction", "Herman Melville", "Moby Dick", "0-553-21311-3", "8.99");
  public static final Basket TOLKEIN = book("fiction", "J. R. R. Tolkein", "The Lord of the Rings", "0-395-19395-8", "22.99");

  public static final Basket RED_BIKE = Raffia.builder().writeStartObject()
      .writeField("color", "red")
      .writeField("price", new BigDecimal("19.95"))
      .writeEndObject()
      .weave();

  public static final Basket store = Raffia.builder().writeStartObject()
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
