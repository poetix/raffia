package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.updaters.Updaters;
import com.codepoetics.raffia.writers.PassThroughWriter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

public class RoundtripTest {

  @Test
  public void readBasket() throws IOException {
    Basket basket = JsonReader.readBasket(getClass().getResourceAsStream("/store.json"));

    assertThat(lens().toAny("author").getAll(Projections.asString, basket),
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

  @Test
  public void rewritePathMatchedStrings() throws IOException {
    Visitor<Basket> toUppercase = Updaters.ofString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.toUpperCase();
      }
    });

    Visitor<Basket> uppercaseTitle = Updaters.updating("title", toUppercase);

    StringWriter stringWriter = new StringWriter();

    FilteringWriter<JsonWriter> transformer = FilteringWriter.rewriting(
        lens("$..book[?]", lens("@.author").matching("Nigel Rees")),
        JsonWriter.writingTo(stringWriter),
        uppercaseTitle
    );

    JsonReader.readWith(getClass().getResourceAsStream("/store.json"), transformer);

    System.out.println(stringWriter.toString());

    assertThat(stringWriter.toString(), containsString("SAYINGS OF THE CENTURY"));
    assertThat(stringWriter.toString(), containsString("The Lord of the Rings"));
  }

  @Ignore
  @Test
  public void projectAuthorsOfCheapBooks() throws IOException {
    FilteringWriter<BasketWeavingWriter> filter = FilteringWriter.projecting(
        lens("$.store.book[?].author", lens("@.price").matchingNumber(NumberPredicates.isLessThan("10")))
    );

    List<String> authors = JsonReader.readWith(getClass().getResourceAsStream("/store.json"), filter)
        .complete()
        .weave()
        .visit(lens("$[*]").gettingAll(Projections.asString));

    assertThat(authors, contains("Nigel Rees", "Herman Melville"));
  }
}
