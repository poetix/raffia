package com.codepoetics.raffia;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.setters.Setters;
import com.codepoetics.raffia.updaters.Updaters;
import org.junit.Test;

import java.math.BigDecimal;

import static com.codepoetics.raffia.StoreExample.store;
import static com.codepoetics.raffia.injections.Injections.fromString;
import static com.codepoetics.raffia.lenses.Lens.lens;
import static com.codepoetics.raffia.projections.Projections.asNumber;
import static com.codepoetics.raffia.projections.Projections.asString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Updating {

  private static final Visitor<Basket> capitaliseString = Updaters.ofString(new Mapper<String, String>() {
    @Override
    public String map(String input) {
      return input.toUpperCase();
    }
  });

  private static final Visitor<Basket> priceToString = Updaters.from(asNumber, new Mapper<BigDecimal, String>() {
    @Override
    public String map(BigDecimal input) {
      return input.toString();
    }
  }, fromString);

  private static final Visitor<Basket> addDescription = Updaters.ofObject(new Mapper<PropertySet, PropertySet>() {
    @Override
    public PropertySet map(PropertySet input) {
      return input.with("description",
          Basket.ofString(
              "\""
                  + input.get("title").visit(asString)
                  + "\", by "
                  + input.get("author").visit(asString)));
    }
  });

  @Test
  public void capitaliseAuthorsOfAllBooks() {
    Basket updated = lens("$..author").update(capitaliseString, store);

    assertThat(
        lens("$..author").getAll(asString, updated),
        contains("NIGEL REES", "EVELYN WAUGH", "HERMAN MELVILLE", "J. R. R. TOLKIEN"));
  }

  @Test
  public void convertPricesToStrings() {
    Basket updated = lens("$..price").update(priceToString, store);

    assertThat(
        lens("$..price").getAll(asString, updated),
        contains("8.95", "12.99", "8.99", "22.99", "19.95"));
  }

  @Test
  public void addDescriptionStringsToBooks() {
    Basket updated = lens("$..book").toAll().update(addDescription, store);

    assertThat(
        lens().toAny("description").getAll(asString, updated),
        hasItem("\"Sayings of the Century\", by Nigel Rees")
    );
  }

  @Test
  public void rewritingAValue() {
    Visitor<Boolean> authorIsNigel = lens("$..author").matching("Nigel Rees");

    Basket updated = lens("$..book[?].title", authorIsNigel)
        .update(Setters.toString("Hallucinogenic Adventures vol. 13"), store);

    assertThat(
        lens("$..book").toMatching(authorIsNigel).to("title").getOne(Projections.asString, updated),
        equalTo("Hallucinogenic Adventures vol. 13"));
  }

}
