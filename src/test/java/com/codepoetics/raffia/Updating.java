package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.PropertySet;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.codepoetics.raffia.StoreExample.*;
import static com.codepoetics.raffia.projections.Projections.asNumber;
import static com.codepoetics.raffia.projections.Projections.asObject;
import static com.codepoetics.raffia.projections.Projections.asString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

public class Updating {

  private static final Visitor<Basket> capitaliseString = Projections.map(asString, new Mapper<String, Basket>() {
    @Override
    public Basket map(String input) {
      return Baskets.ofString(input.toUpperCase());
    }
  });

  private static final Visitor<Basket> priceToString = Projections.map(asNumber, new Mapper<BigDecimal, Basket>() {
    @Override
    public Basket map(BigDecimal input) {
      return Baskets.ofString(input.toString());
    }
  });

  private static final Visitor<Basket> addDescription = Projections.map(asObject, new Mapper<PropertySet, Basket>() {
    @Override
    public Basket map(PropertySet input) {
      return Baskets.ofObject(input.with("description",
          Baskets.ofString(
              "\""
                  + input.get("title").visit(asString)
                  + "\", by "
                  + input.get("author").visit(asString))));
    }
  });

  @Test
  public void capitaliseAuthorsOfAllBooks() {
    Basket updated = Lens.create().toAny("author").update(capitaliseString, store);

    System.out.println(updated);

    assertThat(
        Lens.create().toAny("author").getAll(asString, updated),
        contains("NIGEL REES", "EVELYN WAUGH", "HERMAN MELVILLE", "J. R. R. TOLKEIN"));
  }

  @Test
  public void convertPricesToStrings() {
    Basket updated = Lens.create().toAny("price").update(priceToString, store);

    System.out.println(updated);

    assertThat(
        Lens.create().toAny("price").getAll(asString, updated),
        contains("19.95", "8.95", "12.99", "8.99", "22.99"));
  }

  @Test
  public void addDescriptionStringsToBooks() {
    Basket updated = Lens.create().toAny("book").toAll().update(addDescription, store);

    System.out.println(updated);

    assertThat(
        Lens.create().toAny("description").getAll(asString, updated),
        hasItem("\"Sayings of the Century\", by Nigel Rees")
    );
  }

}
