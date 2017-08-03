package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.writers.PassThroughWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

public class RoundtripTest {

  @Test
  public void readBasket() throws IOException {
    Basket basket = JsonReader.readBasket(getClass().getResourceAsStream("/store.json"));

    assertThat(Lens.lens().toAny("author").getAll(Projections.asString, basket),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));

    String repr = JsonWriter.writeBasketAsString(basket);

    System.out.println(repr);
  }

  @Test
  public void rewriteUrls() throws IOException {
    class UrlRewriter extends PassThroughWriter<JsonWriter, UrlRewriter> {

      protected UrlRewriter(JsonWriter state) {
        super(state);
      }

      @Override
      protected UrlRewriter with(JsonWriter state) {
        return new UrlRewriter(state);
      }

      @Override
      public UrlRewriter add(String value) {
        return super.add(value.replace("DOMAIN", "example.com"));
      }
    }

    StringWriter stringWriter = new StringWriter();
    UrlRewriter writer = new UrlRewriter(JsonWriter.writingTo(stringWriter));

    JsonReader.readWith(getClass().getResourceAsStream("/urls.json"), writer);

    assertThat(stringWriter.toString(), containsString("http://example.com/baz/xyzzy"));
  }
}
