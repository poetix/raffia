package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.PassThroughWriter;
import org.junit.Test;

import java.io.IOException;

import static com.codepoetics.raffia.lenses.Strands.strand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class RoundtripTest {

  @Test
  public void readBasket() throws IOException {
    Basket basket = JsonReader.readBasket(getClass().getResourceAsStream("/store.json"));

    assertThat(strand("$..author").getAllStrings(basket),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));

    String repr = JsonWriter.writeBasketAsString(basket);

    System.out.println(repr);
  }

}
