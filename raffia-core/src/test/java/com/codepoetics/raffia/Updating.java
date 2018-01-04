package com.codepoetics.raffia;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.lenses.Strands;
import com.codepoetics.raffia.operations.Setters;
import com.codepoetics.raffia.operations.Updaters;
import kotlin.jvm.functions.Function1;
import org.junit.Test;

import static com.codepoetics.raffia.StoreExample.store;
import static com.codepoetics.raffia.lenses.Strands.strand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Updating {

  private static final Function1<Basket, Basket> capitaliseString = Updaters.ofString(new Function1<String, String>() {
    @Override
    public String invoke(String input) {
      return input.toUpperCase();
    }
  });

  private static final Function1<Basket, Basket> priceToString = new Function1<Basket, Basket>() {
    @Override
    public Basket invoke(Basket basket) {
      System.out.println(basket);
      return Basket.ofString(basket.asNumber().toString());
    }
  };

  private static final Function1<Basket, Basket> addDescription = Updaters.ofObject(new Function1<PropertySet, PropertySet>() {
    @Override
    public PropertySet invoke(PropertySet input) {
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
    Basket updated = strand("$..author").update(store, capitaliseString);

    assertThat(
        strand("$..author").getAllStrings(updated),
        contains("NIGEL REES", "EVELYN WAUGH", "HERMAN MELVILLE", "J. R. R. TOLKIEN"));
  }

  @Test
  public void convertPricesToStrings() {
    Basket updated = strand("$..price").update(store, priceToString);

    System.out.println(updated);
    assertThat(
        strand("$..price").getAllStrings(updated),
        contains("8.95", "12.99", "8.99", "22.99", "19.95"));
  }

  @Test
  public void addDescriptionStringsToBooks() {
    Basket updated = strand("$..book[*]").update(store, addDescription);

    assertThat(
        strand("$..description").getAllStrings(updated),
        hasItem("\"Sayings of the Century\", by Nigel Rees")
    );
  }

  @Test
  public void rewritingAValue() {
    Function1<Basket, Boolean> authorIsNigel = strand("$..author").matches("Nigel Rees");

    Basket updated = strand("$..book[?].title", authorIsNigel)
        .update(store, Setters.toString("Hallucinogenic Adventures vol. 13"));

    assertThat(
        strand("$..book[?].title", authorIsNigel).getString(updated),
        equalTo("Hallucinogenic Adventures vol. 13"));
  }

}
