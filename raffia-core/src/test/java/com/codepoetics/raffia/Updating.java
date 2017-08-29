package com.codepoetics.raffia;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.functions.Mapper;
import com.codepoetics.raffia.functions.BasketPredicate;
import com.codepoetics.raffia.operations.Setters;
import com.codepoetics.raffia.functions.Updater;
import org.junit.Test;

import static com.codepoetics.raffia.StoreExample.store;
import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Updating {

  private static final Updater capitaliseString = com.codepoetics.raffia.operations.Updaters.INSTANCE.ofString(new Mapper<String, String>() {
    @Override
    public String map(String input) {
      return input.toUpperCase();
    }
  });

  private static final Updater priceToString = new Updater() {
    @Override
    public Basket update(Basket basket) {
      System.out.println(basket);
      return Basket.ofString(basket.asNumber().toString());
    }
  };

  private static final Updater addDescription = com.codepoetics.raffia.operations.Updaters.INSTANCE.ofObject(new Mapper<PropertySet, PropertySet>() {
    @Override
    public PropertySet map(PropertySet input) {
      return input.with("description",
          Basket.ofString(
              "\""
                  + input.get("title").asString()
                  + "\", by "
                  + input.get("author").asString()));
    }
  });

  @Test
  public void capitaliseAuthorsOfAllBooks() {
    Basket updated = lens("$..author").update(capitaliseString, store);

    assertThat(
        lens("$..author").getAllStrings(updated),
        contains("NIGEL REES", "EVELYN WAUGH", "HERMAN MELVILLE", "J. R. R. TOLKIEN"));
  }

  @Test
  public void convertPricesToStrings() {
    Basket updated = lens("$..price").update(priceToString, store);

    System.out.println(updated);
    assertThat(
        lens("$..price").getAllStrings(updated),
        contains("8.95", "12.99", "8.99", "22.99", "19.95"));
  }

  @Test
  public void addDescriptionStringsToBooks() {
    Basket updated = lens("$..book[*]").update(addDescription, store);

    assertThat(
        lens().toAny("description").getAllStrings(updated),
        hasItem("\"Sayings of the Century\", by Nigel Rees")
    );
  }

  @Test
  public void rewritingAValue() {
    BasketPredicate authorIsNigel = lens("$..author").matching("Nigel Rees");

    Basket updated = lens("$..book[?].title", authorIsNigel)
        .update(Setters.INSTANCE.toString("Hallucinogenic Adventures vol. 13"), store);

    assertThat(
        lens("$..book").toMatching(authorIsNigel).to("title").getOne(updated).asString(),
        equalTo("Hallucinogenic Adventures vol. 13"));
  }

}
