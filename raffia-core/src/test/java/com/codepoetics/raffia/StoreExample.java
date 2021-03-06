package com.codepoetics.raffia;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.builders.BasketWeaver;

import java.math.BigDecimal;

public class StoreExample {

  public static final Basket REES = book("reference", "Nigel Rees", "Sayings of the Century", "8.95");
  public static final Basket WAUGH = book("fiction", "Evelyn Waugh", "Sword of Honour", "12.99");
  public static final Basket MELVILLE = book("fiction", "Herman Melville", "Moby Dick", "0-553-21311-3", "8.99");
  public static final Basket TOLKIEN = book("fiction", "J. R. R. Tolkien", "The Lord of the Rings", "0-395-19395-8", "22.99");

  public static final Basket RED_BIKE = Raffia.INSTANCE.weaver().beginObject()
      .add("color", "red")
      .add("price", new BigDecimal("19.95"))
      .end()
      .weave();

  public static final Basket store = Raffia.INSTANCE.weaver().beginObject()
        .key("store")
          .beginObject()
            .addArray("book", REES, WAUGH, MELVILLE, TOLKIEN)
            .addArray("bicycle", RED_BIKE)
          .end()
        .add("expensive", new BigDecimal("10"))
      .end()
      .weave();

  private static BasketWeaver withCommon(String category, String author, String title, BasketWeaver builder) {
    return builder
        .add("category", category)
        .add("author", author)
        .add("title", title);
  }

  private static BasketWeaver withIsbn(String isbn, BasketWeaver writer) {
    return writer.add("isbn", isbn);
  }

  private static BasketWeaver withPrice(String price, BasketWeaver writer) {
    return writer.add("price", new BigDecimal(price));
  }

  private static Basket object(BasketWeaver basketBuilder) {
    return basketBuilder.end().weave();
  }

  private static Basket book(String category, String author, String title, String price) {
    return object(
        withPrice(price,
            withCommon(category, author, title,
                Raffia.INSTANCE.weaver().beginObject())));
  }

  private static Basket book(String category, String author, String title, String isbn, String price) {
    return object(
        withPrice(price,
            withIsbn(isbn,
                withCommon(category, author, title,
                    Raffia.INSTANCE.weaver().beginObject()))));
  }
}
